package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.lib.Outlines;
import mekanism.client.render.lib.Outlines.Line;
import mekanism.client.render.tileentity.RenderSeismicVibrator.VibratorRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderSeismicVibrator extends MekanismTileEntityRenderer<TileEntitySeismicVibrator, VibratorRenderState> implements IWireFrameRenderer {

    @Nullable
    private static List<Line> lines;

    public static void resetCached() {
        lines = null;
    }

    public RenderSeismicVibrator(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public VibratorRenderState createRenderState() {
        return new VibratorRenderState();
    }

    @Override
    public void extractRenderState(TileEntitySeismicVibrator vibrator, VibratorRenderState state, float partialTick, Vec3 cameraPosition,
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(vibrator, state, partialTick, cameraPosition, breakProgress);
        state.piston = Math.max(0, Mth.sin((vibrator.clientPiston + (vibrator.getActive() ? partialTick : 0)) / 5F));
    }

    @Override
    public void submit(VibratorRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0, 0.625 * state.piston, 0);
        //TODO - 26.1: Test crumbling
        nodeCollector.submitBlockModel(
              poseStack,
              Sheets.cutoutBlockSheet(),
              MekanismModelCache.INSTANCE.VIBRATOR_SHAFT.getBakedModel(),
              BlockModelRenderState.EMPTY_TINTS,
              state.lightCoords,
              OverlayTexture.NO_OVERLAY,
              0//No outline
        );
        poseStack.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SEISMIC_VIBRATOR;
    }

    @Override
    public boolean isCombined() {
        return true;
    }

    @Override
    public void renderWireFrame(BlockEntity tile, BlockState blockState, float partialTick, PoseStack poseStack, VertexConsumer buffer, boolean isHighContrast) {
        if (tile instanceof TileEntitySeismicVibrator vibrator) {
            if (lines == null) {
                lines = Outlines.extract(MekanismModelCache.INSTANCE.VIBRATOR_SHAFT.getBakedModel());
            }
            poseStack.pushPose();
            float piston = Math.max(0, (float) Math.sin((vibrator.clientPiston + (vibrator.getActive() ? partialTick : 0)) / 5F));
            poseStack.translate(0, piston * 0.625, 0);
            Pose pose = poseStack.last();
            RenderTickHandler.renderVertexWireFrame(lines, buffer, pose.pose(), pose.normal(), isHighContrast);
            poseStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(TileEntitySeismicVibrator tile) {
        BlockPos pos = tile.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos, pos.above());
    }

    public static class VibratorRenderState extends BlockEntityRenderState {

        public float piston;
    }
}