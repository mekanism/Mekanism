package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.tileentity.RenderIndustrialAlarm.AlarmRenderState;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderIndustrialAlarm extends MekanismTileEntityRenderer<TileEntityIndustrialAlarm, AlarmRenderState> {

    private static final float ROTATE_SPEED = 10F;

    public RenderIndustrialAlarm(BlockEntityRendererProvider.Context context) {
        super(context);
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
        state.setRotation(getAnimationTime(alarm, partialTick) * ROTATE_SPEED % 360);
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
                poseStack.rotate(Axis.XP, Mth.PI);
            }
            case NORTH -> {
                poseStack.translate(0, 0.5, 0.5);
                poseStack.rotate(Axis.XN, Mth.HALF_PI);
            }
            case SOUTH -> {
                poseStack.translate(0, 0.5, -0.5);
                poseStack.rotate(Axis.XP, Mth.HALF_PI);
            }
            case EAST -> {
                poseStack.translate(-0.5, 0.5, 0);
                poseStack.rotate(Axis.ZN, Mth.HALF_PI);
            }
            case WEST -> {
                poseStack.translate(0.5, 0.5, 0);
                poseStack.rotate(Axis.ZP, Mth.HALF_PI);
            }
        }
        poseStack.rotateDegrees(Axis.YP, state.rotation);
        nodeCollector.submitBlockModel(poseStack, Sheets.translucentBlockItemSheet(), MekanismModelCache.INSTANCE.ALARM_BULB.getBakedModel(),
              new int[]{state.tint}, state.lightCoords, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(TileEntityIndustrialAlarm tile, Vec3 camera) {
        return tile.getActive() && super.shouldRender(tile, camera);
    }

    public static class AlarmRenderState extends BlockEntityRenderState {

        @Nullable
        public Direction direction;
        private float rotation;
        private int tint = CommonColors.WHITE;

        public void setRotation(float rotation) {
            this.rotation = rotation;
            //Apply a changing alpha based on how far it is through the rotation
            this.tint = ARGB.white(0.7F + 0.3F * (Math.abs(((this.rotation * 2) % 360) - 180F) / 180F));
        }
    }
}