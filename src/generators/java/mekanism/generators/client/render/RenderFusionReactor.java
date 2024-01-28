package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.render.tileentity.MultiblockTileEntityRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderFusionReactor extends MultiblockTileEntityRenderer<FusionReactorMultiblockData, TileEntityFusionReactorController> {

    private static final double SCALE = 100_000_000;
    private final ModelEnergyCore core;

    public RenderFusionReactor(BlockEntityRendererProvider.Context context) {
        super(context);
        core = new ModelEnergyCore(context.getModelSet());
    }

    @Override
    protected void render(TileEntityFusionReactorController tile, FusionReactorMultiblockData multiblock, float partialTicks, PoseStack matrix, MultiBufferSource renderer,
          int light, int overlayLight, ProfilerFiller profiler) {
        long scaledTemp = Math.round(multiblock.getLastPlasmaTemp() / SCALE);
        float ticks = Minecraft.getInstance().levelRenderer.getTicks() + partialTicks;
        VertexConsumer buffer = renderer.getBuffer(core.RENDER_TYPE);
        matrix.pushPose();
        matrix.translate(0.5, -1.5, 0.5);
        float scale = 1 + 0.7F * sinDegrees(3.14F * scaledTemp + 135);
        renderPart(matrix, buffer, overlayLight, EnumColor.RED, scale, ticks, -6, -7, 0, 36);

        scale = 1 + 0.8F * sinDegrees(3 * scaledTemp);
        renderPart(matrix, buffer, overlayLight, EnumColor.PINK, scale, ticks, 4, 4, 0, 36);

        scale = 1 - 0.9F * sinDegrees(4 * scaledTemp + 90);
        renderPart(matrix, buffer, overlayLight, EnumColor.ORANGE, scale, ticks, 5, -3, -35, 106);

        matrix.popPose();
        endIfNeeded(renderer, core.RENDER_TYPE);
    }

    private static float sinDegrees(float degrees) {
        return Mth.sin((degrees % 360) * Mth.DEG_TO_RAD);
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.FUSION_REACTOR;
    }

    private void renderPart(PoseStack matrix, VertexConsumer buffer, int overlayLight, EnumColor color, float scale, float ticks, int mult1, int mult2,
          int shift1, int shift2) {
        matrix.pushPose();
        matrix.scale(scale, scale, scale);
        matrix.mulPose(Axis.YP.rotationDegrees(ticks * mult1 + shift1));
        matrix.mulPose(RenderEnergyCube.coreVec.rotationDegrees(ticks * mult2 + shift2));
        core.render(matrix, buffer, LightTexture.FULL_BRIGHT, overlayLight, color, 1);
        matrix.popPose();
    }

    @Override
    protected boolean shouldRender(TileEntityFusionReactorController tile, FusionReactorMultiblockData multiblock, Vec3 camera) {
        return multiblock.isBurning();
    }
}