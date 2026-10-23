package com.example.skinears;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * In automatic mode the outer ears use the player skin texture directly.
 * The UV coordinates are taken from the TOP of the head (8..15, 0..7)
 * with the matching hat/hair overlay (40..47, 0..7).
 * This preserves multiple colors and patterns instead of flattening to one color.
 */
public final class EarsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier WHITE =
        Identifier.fromNamespaceAndPath("minecraft", "textures/block/white_concrete.png");

    private final EarModel catBase = new EarModel(makeMesh(true, false));
    private final EarModel catHair = new EarModel(makeMesh(true, true));
    private final EarModel dogBase = new EarModel(makeMesh(false, false));
    private final EarModel dogHair = new EarModel(makeMesh(false, true));
    private final EarModel catInner = new EarModel(makeInnerMesh(true));
    private final EarModel dogInner = new EarModel(makeInnerMesh(false));

    public EarsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible || state.isSpectator) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean local = state.id == mc.player.getId();
        EarsConfig config = SkinEarsClient.config;
        boolean dog = local && "DOG".equals(config.type);
        boolean manual = local && config.customColor;

        EarModel base = dog ? dogBase : catBase;
        EarModel hat = dog ? dogHair : catHair;
        EarModel inner = dog ? dogInner : catInner;
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0f);

        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);

        if (manual) {
            // Manual mode: one user-selected RGB color, using a plain white texture.
            collector.submitModel(base, state, poseStack,
                RenderTypes.entityCutout(WHITE), packedLight, overlay,
                0xFF000000 | (config.color & 0xFFFFFF), null, state.outlineColor);
        } else {
            // Automatic mode: the player's actual skin head-top pixels, not an average.
            Identifier skinTexture = state.skin.body().texturePath();
            collector.submitModel(base, state, poseStack,
                RenderTypes.entityCutout(skinTexture), packedLight, overlay,
                0xFFFFFFFF, null, state.outlineColor);

            // The second skin layer is usually hair/hat. It is often transparent,
            // so render it just outside the base to preserve layered hairstyles.
            if (state.showHat) {
                collector.submitModel(hat, state, poseStack,
                    RenderTypes.entityCutout(skinTexture), packedLight, overlay,
                    0xFFFFFFFF, null, state.outlineColor);
            }
        }

        // A small colored inner surface makes the ears readable on busy skins.
        collector.submitModel(inner, state, poseStack,
            RenderTypes.entityCutout(WHITE), packedLight, overlay,
            manual ? innerColor(config.color) : 0xFFF4A6B8,
            null, state.outlineColor);
        poseStack.popPose();
    }

    private static int innerColor(int color) {
        int r = (((color >> 16) & 255) + 2 * 255) / 3;
        int g = (((color >> 8) & 255) + 2 * 161) / 3;
        int b = ((color & 255) + 2 * 181) / 3;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Each cube is texture-unwrapped into a small window of the top 8x8
     * head-skin square. Overlay cubes shift +32 pixels to the head hat layer.
     */
    private static net.minecraft.client.model.geom.ModelPart makeMesh(boolean cat, boolean hatLayer) {
        int u = hatLayer ? 39 : 7;
        CubeDeformation grow = new CubeDeformation(hatLayer ? 0.035f : 0f);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        if (cat) {
            CubeListBuilder blocks = CubeListBuilder.create()
                .texOffs(u, 0).addBox(-4.05f, -11.0f, -0.95f, 2.95f, 3.1f, 1.8f, grow)
                .texOffs(u, 0).addBox(-3.5f, -12.7f, -0.7f, 1.75f, 1.9f, 1.35f, grow)
                .texOffs(u, 0).addBox(1.1f, -11.0f, -0.95f, 2.95f, 3.1f, 1.8f, grow)
                .texOffs(u, 0).addBox(1.75f, -12.7f, -0.7f, 1.75f, 1.9f, 1.35f, grow);
            root.addOrReplaceChild("cat_ears", blocks, PartPose.ZERO);
        } else {
            // Low, floppy ear flaps extend down beside the head.
            root.addOrReplaceChild("left_flap",
                CubeListBuilder.create().texOffs(u, 0)
                    .addBox(-5.65f, -8.2f, -0.9f, 2.25f, 4.45f, 1.7f, grow),
                PartPose.ZERO);
            root.addOrReplaceChild("right_flap",
                CubeListBuilder.create().texOffs(u, 0)
                    .addBox(3.4f, -8.2f, -0.9f, 2.25f, 4.45f, 1.7f, grow),
                PartPose.ZERO);

            // Rotated upper sections curve inward toward the crown/head.
            // Left = positive Z roll; right = negative Z roll.
            root.addOrReplaceChild("left_fold",
                CubeListBuilder.create().texOffs(u, 0)
                    .addBox(-1.02f, -2.6f, -0.95f, 2.05f, 2.7f, 1.8f, grow),
                PartPose.offsetAndRotation(-4.42f, -8.1f, 0f, 0f, 0f, 0.64f));
            root.addOrReplaceChild("right_fold",
                CubeListBuilder.create().texOffs(u, 0)
                    .addBox(-1.02f, -2.6f, -0.95f, 2.05f, 2.7f, 1.8f, grow),
                PartPose.offsetAndRotation(4.42f, -8.1f, 0f, 0f, 0f, -0.64f));
        }

        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    private static net.minecraft.client.model.geom.ModelPart makeInnerMesh(boolean cat) {
        MeshDefinition mesh = new MeshDefinition();
        if (cat) {
            mesh.getRoot().addOrReplaceChild("inner",
                CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-3.55f, -10.85f, -1.15f, 1.45f, 2.05f, 0.24f)
                    .addBox(2.10f, -10.85f, -1.15f, 1.45f, 2.05f, 0.24f),
                PartPose.ZERO);
        } else {
            mesh.getRoot().addOrReplaceChild("inner",
                CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-5.32f, -7.6f, -1.12f, 1.55f, 3.25f, 0.24f)
                    .addBox(3.77f, -7.6f, -1.12f, 1.55f, 3.25f, 0.24f),
                PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    private static final class EarModel extends EntityModel<AvatarRenderState> {
        private EarModel(net.minecraft.client.model.geom.ModelPart root) {
            super(root);
        }
    }
}
