package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
public class RenderFusionReactor extends MekanismTileEntityRenderer<TileEntityFusionReactorController> {

    private static final double SCALE = 100_000_000;
    private final ModelEnergyCore core;

    public RenderFusionReactor(BlockEntityRendererProvider.Context context) {
        super(context);
        core = new ModelEnergyCore(context.getModelSet());
    }

    @Override
    protected void render(TileEntityFusionReactorController tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        FusionReactorMultiblockData multiblock = tile.getMultiblock();
        if (multiblock.isFormed() && multiblock.isBurning()) {
            matrix.pushPose();
            matrix.translate(0.5, -1.5, 0.5);

            long scaledTemp = Math.round(multiblock.getLastPlasmaTemp() / SCALE);
            float ticks = MekanismClient.ticksPassed + partialTick;
            double scale = 1 + 0.7 * Math.sin(Math.toRadians(ticks * 3.14 * scaledTemp + 135F));
            VertexConsumer buffer = core.getBuffer(renderer);
            renderPart(matrix, buffer, overlayLight, EnumColor.AQUA, scale, ticks, scaledTemp, -6, -7, 0, 36);

            scale = 1 + 0.8 * Math.sin(Math.toRadians(ticks * 3 * scaledTemp));
            renderPart(matrix, buffer, overlayLight, EnumColor.RED, scale, ticks, scaledTemp, 4, 4, 0, 36);

            scale = 1 - 0.9 * Math.sin(Math.toRadians(ticks * 4 * scaledTemp + 90F));
            renderPart(matrix, buffer, overlayLight, EnumColor.ORANGE, scale, ticks, scaledTemp, 5, -3, -35, 106);

            matrix.popPose();
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.FUSION_REACTOR;
    }

    private void renderPart(PoseStack matrix, VertexConsumer buffer, int overlayLight, EnumColor color, double scale, float ticks, long scaledTemp, int mult1,
          int mult2, int shift1, int shift2) {
        float ticksScaledTemp = ticks * scaledTemp;
        matrix.pushPose();
        matrix.scale((float) scale, (float) scale, (float) scale);
        matrix.mulPose(Vector3f.YP.rotationDegrees(ticksScaledTemp * mult1 + shift1));
        matrix.mulPose(RenderEnergyCube.coreVec.rotationDegrees(ticksScaledTemp * mult2 + shift2));
        core.render(matrix, buffer, MekanismRenderer.FULL_LIGHT, overlayLight, color, 1);
        matrix.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityFusionReactorController tile) {
        FusionReactorMultiblockData multiblock = tile.getMultiblock();
        return multiblock.isFormed() && multiblock.isBurning();
    }
}