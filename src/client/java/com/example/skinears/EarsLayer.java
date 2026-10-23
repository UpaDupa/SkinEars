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
            // Narrow upright ears like the supplied Minecraft reference,
            // with a short tapered crown and subtly inward-facing angles.
            root.addOrReplaceChild("cat_left_ear",
                CubeListBuilder.create().texOffs(u, 0)
                    .addBox(-0.95f, -3.15f, -0.85f, 1.9f, 3.25f, 1.7f, grow)
                    .texOffs(u, 0)
                    .addBox(-0.75f, -4.25f, -0.7f, 1.5f, 1.2f, 1.4f, grow),
                PartPose.offsetAndRotation(-2.55f, -8.6f, 0f, 0f, 0f, 0.14f));
            root.addOrReplaceChild("cat_right_ear",
                CubeListBuilder.create().texOffs(u, 0)
                    .addBox(-0.95f, -3.15f, -0.85f, 1.9f, 3.25f, 1.7f, grow)
                    .texOffs(u, 0)
                    .addBox(-0.75f, -4.25f, -0.7f, 1.5f, 1.2f, 1.4f, grow),
                PartPose.offsetAndRotation(2.55f, -8.6f, 0f, 0f, 0f, -0.14f));
        } else {
            // Broad, semi-upright husky ears: taller and slightly farther apart.
            root.addOrReplaceChild("dog_left_ear",
                CubeListBuilder.create().texOffs(u, 0)
                    .addBox(-1.35f, -3.55f, -0.95f, 2.7f, 3.9f, 1.9f, grow)
                    .texOffs(u, 0)
                    .addBox(-1.0f, -5.0f, -0.75f, 2.0f, 1.6f, 1.5f, grow),
                PartPose.offsetAndRotation(-3.35f, -8.7f, 0f, 0f, 0f, 0.18f));
            root.addOrReplaceChild("dog_right_ear",
                CubeListBuilder.create().texOffs(u, 0)
                    .addBox(-1.35f, -3.55f, -0.95f, 2.7f, 3.9f, 1.9f, grow)
                    .texOffs(u, 0)
                    .addBox(-1.0f, -5.0f, -0.75f, 2.0f, 1.6f, 1.5f, grow),
                PartPose.offsetAndRotation(3.35f, -8.7f, 0f, 0f, 0f, -0.18f));
        }

        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    private static net.minecraft.client.model.geom.ModelPart makeInnerMesh(boolean cat) {
        MeshDefinition mesh = new MeshDefinition();
        if (cat) {
            // Tall, narrow pink highlights on the front, matching the screenshot.
            mesh.getRoot().addOrReplaceChild("cat_left_inner",
                CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-0.46f, -2.35f, -1.03f, 0.92f, 2.25f, 0.20f),
                PartPose.offsetAndRotation(-2.55f, -8.6f, 0f, 0f, 0f, 0.14f));
            mesh.getRoot().addOrReplaceChild("cat_right_inner",
                CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-0.46f, -2.35f, -1.03f, 0.92f, 2.25f, 0.20f),
                PartPose.offsetAndRotation(2.55f, -8.6f, 0f, 0f, 0f, -0.14f));
        } else {
            mesh.getRoot().addOrReplaceChild("dog_left_inner",
                CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-0.62f, -2.6f, -1.08f, 1.24f, 2.9f, 0.20f),
                PartPose.offsetAndRotation(-3.35f, -8.7f, 0f, 0f, 0f, 0.18f));
            mesh.getRoot().addOrReplaceChild("dog_right_inner",
                CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-0.62f, -2.6f, -1.08f, 1.24f, 2.9f, 0.20f),
                PartPose.offsetAndRotation(3.35f, -8.7f, 0f, 0f, 0f, -0.18f));
        }

        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    private static final class EarModel extends EntityModel<AvatarRenderState> {
        private EarModel(net.minecraft.client.model.geom.ModelPart root) {
            super(root);
        }
    }
}
