package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.ModelSeismicVibrator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;

@NothingNullByDefault
public class RenderSeismicVibrator extends ModelTileEntityRenderer<TileEntitySeismicVibrator, ModelSeismicVibrator> implements IWireFrameRenderer {

    public RenderSeismicVibrator(BlockEntityRendererProvider.Context context) {
        super(context, ModelSeismicVibrator::new);
    }

    @Override
    protected void render(TileEntitySeismicVibrator tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        renderTranslated(tile, partialTick, matrix, (poseStack, actualRate) -> model.render(poseStack, renderer, light, overlayLight, actualRate, false));
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SEISMIC_VIBRATOR;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntitySeismicVibrator tile) {
        return true;
    }

    @Override
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer, int red, int green, int blue, int alpha) {
        if (tile instanceof TileEntitySeismicVibrator vibrator) {
            renderTranslated(vibrator, partialTick, matrix, (poseStack, actualRate) -> model.renderWireFrame(poseStack, buffer, actualRate, red, green, blue, alpha));
        }
    }

    private void renderTranslated(TileEntitySeismicVibrator tile, float partialTick, PoseStack matrix, VibratorRenderer renderer) {
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        float actualRate = Math.max(0, (float) Math.sin((tile.clientPiston + (tile.getActive() ? partialTick : 0)) / 5F));
        renderer.render(matrix, actualRate);
        matrix.popPose();
    }

    private interface VibratorRenderer {

        void render(PoseStack poseStack, float actualRate);
    }
}