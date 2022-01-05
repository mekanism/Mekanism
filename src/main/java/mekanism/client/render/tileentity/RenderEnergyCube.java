package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
public class RenderEnergyCube extends MekanismTileEntityRenderer<TileEntityEnergyCube> {

    public static final Vector3f coreVec = new Vector3f(0.0F, MekanismUtils.ONE_OVER_ROOT_TWO, MekanismUtils.ONE_OVER_ROOT_TWO);
    private final ModelEnergyCube model;
    private final ModelEnergyCore core;

    public RenderEnergyCube(BlockEntityRendererProvider.Context context) {
        super(context);
        model = new ModelEnergyCube(context.getModelSet());
        core = new ModelEnergyCore(context.getModelSet());
    }

    @Override
    protected void render(TileEntityEnergyCube tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        profiler.push(ProfilerConstants.FRAME);
        matrix.pushPose();
        matrix.translate(0.5, 1.5, 0.5);
        switch (tile.getDirection()) {
            case DOWN -> {
                matrix.mulPose(Vector3f.XN.rotationDegrees(90));
                matrix.translate(0, 1, -1);
            }
            case UP -> {
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
                matrix.translate(0, 1, 1);
            }
            //Otherwise, use the helper method for handling different face options because it is one of them
            default -> MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        }
        matrix.mulPose(Vector3f.ZP.rotationDegrees(180));
        profiler.push(ProfilerConstants.CORNERS);
        model.render(matrix, renderer, light, overlayLight, tile.getTier(), false, false);
        profiler.popPush(ProfilerConstants.SIDES);
        model.renderSidesBatched(tile, matrix, renderer, light, overlayLight);
        profiler.pop();//End sides
        matrix.popPose();

        profiler.popPush(ProfilerConstants.CORE);//End frame start core
        float energyScale = tile.getEnergyScale();
        if (energyScale > 0) {
            matrix.pushPose();
            matrix.translate(0.5, 0.5, 0.5);
            float ticks = MekanismClient.ticksPassed + partialTick;
            matrix.scale(0.4F, 0.4F, 0.4F);
            matrix.translate(0, Math.sin(Math.toRadians(3 * ticks)) / 7, 0);
            float scaledTicks = 4 * ticks;
            matrix.mulPose(Vector3f.YP.rotationDegrees(scaledTicks));
            matrix.mulPose(coreVec.rotationDegrees(36F + scaledTicks));
            core.render(matrix, renderer, MekanismRenderer.FULL_LIGHT, overlayLight, tile.getTier().getBaseTier().getColor(), energyScale);
            matrix.popPose();
        }
        profiler.pop();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.ENERGY_CUBE;
    }
}