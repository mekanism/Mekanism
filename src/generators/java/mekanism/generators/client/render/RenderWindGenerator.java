package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.IWireFrameRenderer;
import mekanism.client.render.tileentity.ModelTileEntityRenderer;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;

@NothingNullByDefault
public class RenderWindGenerator extends ModelTileEntityRenderer<TileEntityWindGenerator, ModelWindGenerator> implements IWireFrameRenderer {

    public RenderWindGenerator(BlockEntityRendererProvider.Context context) {
        super(context, ModelWindGenerator::new);
    }

    @Override
    protected void render(TileEntityWindGenerator tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        renderTranslated(tile, partialTick, matrix, (poseStack, angle) -> model.render(poseStack, renderer, angle, light, overlayLight, false));
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.WIND_GENERATOR;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityWindGenerator tile) {
        return true;
    }

    @Override
    public void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer, int red, int green, int blue, int alpha) {
        if (tile instanceof TileEntityWindGenerator windGenerator) {
            renderTranslated(windGenerator, partialTick, matrix, (poseStack, angle) -> model.renderWireFrame(poseStack, buffer, angle, red, green, blue, alpha));
        }
    }

    private void renderTranslated(TileEntityWindGenerator tile, float partialTick, PoseStack matrix, WindGeneratorRenderer renderer) {
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        double angle = tile.getAngle();
        if (tile.getActive()) {
            angle = (tile.getAngle() + ((tile.getBlockPos().getY() + 4F) / TileEntityWindGenerator.SPEED_SCALED) * partialTick) % 360;
        }
        renderer.render(matrix, angle);
        matrix.popPose();
    }

    private interface WindGeneratorRenderer {

        void render(PoseStack poseStack, double angle);
    }
}