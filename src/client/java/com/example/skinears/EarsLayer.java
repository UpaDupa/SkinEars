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
 * Cat and husky ears tilt outward. Outer and inner sections can use
 * the head-top skin texture or a color picked from a skin pixel.
 * The inside of the ears also supports the default soft pink.
 */
public final class EarsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier WHITE =
        Identifier.fromNamespaceAndPath("skinears", "textures/white.png");

    private final EarModel[][] outer = new EarModel[2][2];
    private final EarModel[][] inner = new EarModel[2][2];
    private final EarModel[][] headOuter = new EarModel[2][2];
    private final EarModel[][] headInner = new EarModel[2][2];

    public EarsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
        for (int shape=0;shape<2;shape++)
            for (int side=0;side<2;side++) {
                outer[shape][side] = new EarModel(mesh(shape==0,side,false,false));
                inner[shape][side] = new EarModel(mesh(shape==0,side,true,false));
                headOuter[shape][side] = new EarModel(mesh(shape==0,side,false,true));
                headInner[shape][side] = new EarModel(mesh(shape==0,side,true,true));
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
        poses.pushPose();
        getParentModel().head.translateAndRotate(poses);
        Identifier skin=state.skin.body().texturePath();
        String outerMode=local?settings.outerMode:"HEAD";
        String innerMode=local?settings.innerMode:"PINK";
        boolean outsideHead="HEAD".equals(outerMode);
        boolean insideHead="HEAD".equals(innerMode);

        for(int side=0;side<2;side++){
            int outerRgb=0xFFFFFF;
            int innerRgb=0xF4A6B8;

            if(local && "PICKED".equals(outerMode)){
                boolean selected=side==0?settings.outerLeftPicked:settings.outerRightPicked;
                if(!selected) {
                    // Picking just one ear applies it to both until the other is chosen.
                    selected=side==0?settings.outerRightPicked:settings.outerLeftPicked;
                    if(selected){
                        int sx=side==0?settings.outerRightX:settings.outerLeftX;
                        int sy=side==0?settings.outerRightY:settings.outerLeftY;
                        int saved=side==0?settings.outerRightPickedColor:settings.outerLeftPickedColor;
                        int live=SkinEarsClient.COLORS.pixel(state.skin,sx,sy);
                        outerRgb=live>=0?live:saved;
                    }
                } else {
                    int sx=side==0?settings.outerLeftX:settings.outerRightX;
                    int sy=side==0?settings.outerLeftY:settings.outerRightY;
                    int saved=side==0?settings.outerLeftPickedColor:settings.outerRightPickedColor;
                    int live=SkinEarsClient.COLORS.pixel(state.skin,sx,sy);
                    outerRgb=live>=0?live:saved;
                }
            }

            if(local && "PICKED".equals(innerMode)){
                boolean selected=side==0?settings.innerLeftPicked:settings.innerRightPicked;
                if(!selected) {
                    selected=side==0?settings.innerRightPicked:settings.innerLeftPicked;
                    if(selected){
                        int sx=side==0?settings.innerRightX:settings.innerLeftX;
                        int sy=side==0?settings.innerRightY:settings.innerLeftY;
                        int saved=side==0?settings.innerRightPickedColor:settings.innerLeftPickedColor;
                        int live=SkinEarsClient.COLORS.pixel(state.skin,sx,sy);
                        innerRgb=live>=0?live:saved;
                    }
                } else {
                    int sx=side==0?settings.innerLeftX:settings.innerRightX;
                    int sy=side==0?settings.innerLeftY:settings.innerRightY;
                    int saved=side==0?settings.innerLeftPickedColor:settings.innerRightPickedColor;
                    int live=SkinEarsClient.COLORS.pixel(state.skin,sx,sy);
                    innerRgb=live>=0?live:saved;
                }
            }

            collector.submitModel(outsideHead?headOuter[shape][side]:outer[shape][side],
                state,poses,RenderTypes.entityCutout(outsideHead?skin:WHITE),light,overlay,
                outsideHead?0xFFFFFFFF:0xFF000000|(outerRgb&0xFFFFFF),null,state.outlineColor);
            collector.submitModel(insideHead?headInner[shape][side]:inner[shape][side],
                state,poses,RenderTypes.entityCutout(insideHead?skin:WHITE),light,overlay,
                insideHead?0xFFFFFFFF:0xFF000000|(innerRgb&0xFFFFFF),null,state.outlineColor);
        }
        poses.popPose();
    }

    /** Models for the outer and inner surfaces, tilted outwards. */
    private static net.minecraft.client.model.geom.ModelPart mesh(
            boolean cat, int side, boolean inside, boolean headTexture) {
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition root=mesh.getRoot();
        float x=side==0 ? -2.7f : 2.7f;
        float angle=side==0 ? -0.22f : 0.22f;
        CubeListBuilder cubes=CubeListBuilder.create().texOffs(headTexture?7:0,0);
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
                        0f,0f,side==0?-0.25f:0.25f));
        }
        return LayerDefinition.create(mesh,64,64).bakeRoot();
    }

    private static final class EarModel extends EntityModel<AvatarRenderState> {
        private EarModel(net.minecraft.client.model.geom.ModelPart root) {
            super(root);
        }
    }
}
