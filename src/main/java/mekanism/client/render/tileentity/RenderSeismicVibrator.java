package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.ModelSeismicVibrator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;

@ParametersAreNonnullByDefault
public class RenderSeismicVibrator extends MekanismTileEntityRenderer<TileEntitySeismicVibrator> implements IWireFrameRenderer {

    private final ModelSeismicVibrator model;

    public RenderSeismicVibrator(BlockEntityRendererProvider.Context context) {
        super(context);
        model = new ModelSeismicVibrator(context.getModelSet());
    }

    @Override
    protected void render(TileEntitySeismicVibrator tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        float actualRate = performTranslationsAndGetRate(tile, partialTick, matrix);
        model.render(matrix, renderer, light, overlayLight, actualRate, false);
        matrix.popPose();
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
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer, float red, float green, float blue, float alpha) {
        if (tile instanceof TileEntitySeismicVibrator vibrator) {
            float actualRate = performTranslationsAndGetRate(vibrator, partialTick, matrix);
            model.renderWireFrame(matrix, buffer, actualRate, red, green, blue, alpha);
            matrix.popPose();
        }
    }

    /**
     * Make sure to call {@link PoseStack#popPose()} afterwards
     */
    private float performTranslationsAndGetRate(TileEntitySeismicVibrator tile, float partialTick, PoseStack matrix) {
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        return Math.max(0, (float) Math.sin((tile.clientPiston + (tile.getActive() ? partialTick : 0)) / 5F));
    }
}