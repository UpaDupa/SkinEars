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

    private final EarModel[][][] outer = new EarModel[2][2][SkinRegion.values().length];
    private final EarModel[][][] inner = new EarModel[2][2][SkinRegion.values().length];

    public EarsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
        for (int shape=0;shape<2;shape++)
            for (int side=0;side<2;side++)
                for (SkinRegion region : SkinRegion.values()) {
                    outer[shape][side][region.ordinal()] =
                        new EarModel(mesh(shape == 0, side, region.u, region.v, false));
                    inner[shape][side][region.ordinal()] =
                        new EarModel(mesh(shape == 0, side, region.u, region.v, true));
                }
    }

    @Override
    public void submit(PoseStack poses, SubmitNodeCollector collector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible || state.isSpectator) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        EarsConfig settings = SkinEarsClient.config;
        boolean local = state.id == mc.player.getId();
        boolean dog = local && "DOG".equals(settings.type);
        int shape = dog ? 1 : 0;
        int overlay = LivingEntityRenderer.getOverlayCoords(state, 0f);
        Identifier skin = state.skin.body().texturePath();
        poses.pushPose();
        getParentModel().head.translateAndRotate(poses);

        for (int side=0;side<2;side++) {
            int regionIndex = side == 0 ? settings.outerLeftRegion : settings.outerRightRegion;
            int innerIndex = side == 0 ? settings.innerLeftRegion : settings.innerRightRegion;
            if (!local) {regionIndex=0;innerIndex=0;}
            regionIndex=Math.floorMod(regionIndex,SkinRegion.values().length);
            innerIndex=Math.floorMod(innerIndex,SkinRegion.values().length);
            boolean pickedOuter = local && !settings.customColor &&
                (side==0 ? settings.outerLeftPicked:settings.outerRightPicked);
            int pickX=side==0?settings.outerLeftX:settings.outerRightX;
            int pickY=side==0?settings.outerLeftY:settings.outerRightY;
            int pickedOuterColor=pickedOuter ?
                SkinEarsClient.COLORS.pixel(state.skin,pickX,pickY) : -1;

            boolean outerTinted=local && settings.customColor || pickedOuterColor>=0;
            int outerColor=local && settings.customColor ? settings.color :
                pickedOuterColor>=0 ? pickedOuterColor : 0xFFFFFF;
            Identifier outerTexture=outerTinted?WHITE:skin;
            collector.submitModel(outer[shape][side][regionIndex], state, poses,
                    RenderTypes.entityCutout(outerTexture), light, overlay,
                    0xFF000000 | (outerColor&0xFFFFFF), null, state.outlineColor);

            String mode=local?settings.innerMode:"PINK";
            boolean pickInner="PICKED".equals(mode) &&
                (side==0?settings.innerLeftPicked:settings.innerRightPicked);
            int ix=side==0?settings.innerLeftX:settings.innerRightX;
            int iy=side==0?settings.innerLeftY:settings.innerRightY;
            int sampledInner=pickInner?
                SkinEarsClient.COLORS.pixel(state.skin,ix,iy):-1;
            // Both the whole-region texture and an exact sampled pixel are supported.
            boolean copiedRegion="SKIN".equals(mode);
            Identifier innerTexture=copiedRegion?skin:WHITE;
            int innerColor="CUSTOM".equals(mode)?settings.innerColor:
                sampledInner>=0?sampledInner:0xF4A6B8;
            collector.submitModel(inner[shape][side][innerIndex], state, poses,
                    RenderTypes.entityCutout(innerTexture), light, overlay,
                    copiedRegion?0xFFFFFFFF:0xFF000000|(innerColor&0xFFFFFF),
                    null, state.outlineColor);
        }
        poses.popPose();
    }

    /**
     * One independent mesh for each ear and region. Each uses the texture
     * coordinates of the selected region on the player's original skin.
     */
    private static net.minecraft.client.model.geom.ModelPart mesh(
            boolean cat, int side, int u, int v, boolean inside) {
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition root=mesh.getRoot();
        float x=side==0 ? -2.7f : 2.7f;
        float angle=side==0 ? 0.12f : -0.12f;
        CubeListBuilder cubes=CubeListBuilder.create().texOffs(u,v);
        if(cat) {
            // Restored squat cat ears: wider than the old narrow reference shape
            // and shorter than the old tall cat ears.
            if(inside) cubes.addBox(-0.72f,-2.0f,-1.08f,1.44f,1.95f,0.23f);
            else {
                cubes.addBox(-1.45f,-2.75f,-0.95f,2.9f,2.95f,1.9f);
                cubes.addBox(-1.04f,-3.95f,-0.78f,2.08f,1.35f,1.56f);
            }
            root.addOrReplaceChild("ear",cubes,
                    PartPose.offsetAndRotation(x,-8.1f,0f,0f,0f,angle));
        } else {
            // Husky-style, semi-upright ears.
            if(inside) cubes.addBox(-0.66f,-2.65f,-1.11f,1.32f,2.8f,0.23f);
            else {
                cubes.addBox(-1.35f,-3.55f,-0.95f,2.7f,3.9f,1.9f);
                cubes.addBox(-1.0f,-5f,-0.75f,2f,1.6f,1.5f);
            }
            root.addOrReplaceChild("ear",cubes,
                    PartPose.offsetAndRotation(side==0?-3.35f:3.35f,-8.7f,0f,
                        0f,0f,side==0?0.18f:-0.18f));
        }
        return LayerDefinition.create(mesh,64,64).bakeRoot();
    }

    private static final class EarModel extends EntityModel<AvatarRenderState> {
        private EarModel(net.minecraft.client.model.geom.ModelPart root) {
            super(root);
        }
    }
}
