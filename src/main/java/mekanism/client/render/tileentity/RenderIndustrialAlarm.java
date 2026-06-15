package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.model.ModelIndustrialAlarm;
import mekanism.client.model.ModelIndustrialAlarm.IndustrialAlarmRenderState;
import mekanism.client.render.tileentity.RenderIndustrialAlarm.AlarmRenderState;
import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderIndustrialAlarm extends MekanismTileEntityRenderer<TileEntityIndustrialAlarm, AlarmRenderState> {

    public static final ModelLayerLocation LIGHT_BOX_LAYER = new ModelLayerLocation(Mekanism.rl("industrial_alarm/light_box"), "main");
    private static final float ROTATE_SPEED = 10F;

    public static LayerDefinition createLightBoxLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("light_box",
              CubeListBuilder.create().addBox(-2F, 1F, -2F, 4, 4, 4, new CubeDeformation(0.01F)),
              PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    private final ModelIndustrialAlarm model;
    private final ModelPart lightBox;

    public RenderIndustrialAlarm(BlockEntityRendererProvider.Context context) {
        super(context);
        this.model = new ModelIndustrialAlarm(context.entityModelSet());
        this.lightBox = context.bakeLayer(LIGHT_BOX_LAYER);
    }

    @Override
    public AlarmRenderState createRenderState() {
        return new AlarmRenderState();
    }

    @Override
    public void extractRenderState(TileEntityIndustrialAlarm alarm, AlarmRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(alarm, state, partialTick, cameraPosition, breakProgress);
        state.direction = alarm.getDirection();
        //TODO - 26.1: Do we want to use game time as a basis or some other value?
        state.modelState.setRotation((alarm.getGameTime() + partialTick) * ROTATE_SPEED % 360);
    }

    @Override
    public void submit(AlarmRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.direction == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        switch (state.direction) {
            case DOWN -> {
                poseStack.translate(0, 1, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
            }
            case NORTH -> {
                poseStack.translate(0, 0.5, 0.5);
                poseStack.mulPose(Axis.XN.rotationDegrees(90));
            }
            case SOUTH -> {
                poseStack.translate(0, 0.5, -0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            case EAST -> {
                poseStack.translate(-0.5, 0.5, 0);
                poseStack.mulPose(Axis.ZN.rotationDegrees(90));
            }
            case WEST -> {
                poseStack.translate(0.5, 0.5, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            }
        }
        nodeCollector.submitModel(this.model, state.modelState, poseStack, this.model.getRenderType(), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        RenderType renderType = this.model.getRenderType();
        nodeCollector.submitModelPart(
              this.lightBox,
              poseStack,
              renderType,
              LightCoordsUtil.FULL_BRIGHT,
              OverlayTexture.NO_OVERLAY,
              null
        );
        poseStack.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.INDUSTRIAL_ALARM;
    }

    @Override
    public boolean shouldRender(TileEntityIndustrialAlarm tile, Vec3 camera) {
        return tile.getActive() && super.shouldRender(tile, camera);
    }

    public static class AlarmRenderState extends BlockEntityRenderState {

        @Nullable
        public Direction direction;
        public IndustrialAlarmRenderState modelState = new IndustrialAlarmRenderState();
    }
}