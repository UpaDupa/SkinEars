package com.example.skinears;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Two different head-following ear meshes; tinted by sampled or custom skin color. */
public final class EarsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath("skinears", "textures/white.png");

    private final EarModel catOuter = new EarModel(makeMesh(true, false));
    private final EarModel catInner = new EarModel(makeMesh(true, true));
    private final EarModel dogOuter = new EarModel(makeMesh(false, false));
    private final EarModel dogInner = new EarModel(makeMesh(false, true));

    public EarsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible || state.isSpectator) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Skin-specific automatic color is used for ALL players locally.
        int sampled = SkinEarsClient.COLORS.colorFor(state.skin);
        boolean local = state.id == mc.player.getId();
        EarsConfig settings = SkinEarsClient.config;
        boolean dog = local && "DOG".equals(settings.type);
        int color = local && settings.customColor ? settings.color : sampled;
        if (local) SkinEarsClient.COLORS.setLatestOwnColor(sampled);

        int outerColor = 0xFF000000 | (color & 0xFFFFFF);
        int innerColor = innerColor(color);
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0f);

        poseStack.pushPose();
        // Mirrors Minecraft's helmet / player-ear layers so the mesh follows
        // head rotation, sneaking, swimming and animations naturally.
        getParentModel().head.translateAndRotate(poseStack);

        collector.submitModel(dog ? dogOuter : catOuter, state, poseStack,
                RenderTypes.entityCutout(WHITE), packedLight, overlay,
                outerColor, null, state.outlineColor);
        collector.submitModel(dog ? dogInner : catInner, state, poseStack,
                RenderTypes.entityCutout(WHITE), packedLight, overlay,
                innerColor, null, state.outlineColor);
        poseStack.popPose();
    }

    private static int innerColor(int c) {
        // Pinkish inner ear blended with current skin hue.
        int r = (((c >> 16) & 255) + 2 * 255) / 3;
        int g = (((c >> 8) & 255) + 2 * 161) / 3;
        int b = ((c & 255) + 2 * 181) / 3;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static net.minecraft.client.model.geom.ModelPart makeMesh(boolean cat, boolean inner) {
        MeshDefinition mesh = new MeshDefinition();
        CubeListBuilder boxes = CubeListBuilder.create().texOffs(0, 0);
        if (cat) {
            if (inner) {
                // Two front-facing pink stripes, set just in front of the outer ears.
                boxes.addBox(-3.65f, -11.25f, -1.34f, 1.55f, 2.25f, 0.3f);
                boxes.addBox( 2.10f, -11.25f, -1.34f, 1.55f, 2.25f, 0.3f);
            } else {
                // Squarish lower portion plus smaller raised tips reads as pointed cat ears.
                boxes.addBox(-4.20f, -11.25f, -1.10f, 3.10f, 3.35f, 2.0f);
                boxes.addBox(-3.50f, -13.00f, -0.85f, 1.70f, 2.0f, 1.5f);
                boxes.addBox( 1.10f, -11.25f, -1.10f, 3.10f, 3.35f, 2.0f);
                boxes.addBox( 1.80f, -13.00f, -0.85f, 1.70f, 2.0f, 1.5f);
            }
        } else {
            if (inner) {
                // Floppy dog ears drop down beside the sides of the face.
                boxes.addBox(-6.1f, -7.35f, -1.31f, 1.35f, 3.4f, 0.3f);
                boxes.addBox( 4.75f, -7.35f, -1.31f, 1.35f, 3.4f, 0.3f);
            } else {
                boxes.addBox(-6.60f, -9.25f, -1.10f, 3.0f, 5.7f, 2.0f);
                boxes.addBox(-6.75f, -5.55f, -1.0f, 2.8f, 2.2f, 1.8f);
                boxes.addBox( 3.60f, -9.25f, -1.10f, 3.0f, 5.7f, 2.0f);
                boxes.addBox( 3.95f, -5.55f, -1.0f, 2.8f, 2.2f, 1.8f);
            }
        }
        mesh.getRoot().addOrReplaceChild("ears", boxes, PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    private static final class EarModel extends EntityModel<AvatarRenderState> {
        private EarModel(net.minecraft.client.model.geom.ModelPart root) {
            super(root);
        }
    }
}
