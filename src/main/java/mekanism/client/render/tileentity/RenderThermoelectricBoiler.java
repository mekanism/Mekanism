package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.ChemicalRenderData.GasRenderData;
import mekanism.client.render.data.FluidRenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
public class RenderThermoelectricBoiler extends MekanismTileEntityRenderer<TileEntityBoilerCasing> {

    public RenderThermoelectricBoiler(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityBoilerCasing tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        if (tile.isMaster()) {
            BoilerMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed() && multiblock.renderLocation != null && multiblock.upperRenderLocation != null) {
                BlockPos pos = tile.getBlockPos();
                VertexConsumer buffer = null;
                if (!multiblock.waterTank.isEmpty()) {
                    int height = multiblock.upperRenderLocation.getY() - 1 - multiblock.renderLocation.getY();
                    if (height >= 1) {
                        FluidRenderData data = new FluidRenderData(multiblock.waterTank.getFluid());
                        data.location = multiblock.renderLocation;
                        data.height = height;
                        data.length = multiblock.length();
                        data.width = multiblock.width();
                        int glow = data.calculateGlowLight(MekanismRenderer.FULL_SKY_LIGHT);
                        matrix.pushPose();
                        matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
                        buffer = renderer.getBuffer(Sheets.translucentCullBlockSheet());
                        Model3D model = ModelRenderer.getModel(data, multiblock.prevWaterScale);
                        MekanismRenderer.renderObject(model, matrix, buffer, data.getColorARGB(multiblock.prevWaterScale), glow, overlayLight, getFaceDisplay(data, model));
                        matrix.popPose();
                        MekanismRenderer.renderValves(matrix, buffer, multiblock.valves, data, pos, glow, overlayLight, isInsideMultiblock(data));
                    }
                }
                if (!multiblock.steamTank.isEmpty()) {
                    int height = multiblock.renderLocation.getY() + multiblock.height() - 2 - multiblock.upperRenderLocation.getY();
                    if (height >= 1) {
                        GasRenderData data = new GasRenderData(multiblock.steamTank.getStack());
                        data.location = multiblock.upperRenderLocation;
                        data.height = height;
                        data.length = multiblock.length();
                        data.width = multiblock.width();
                        if (buffer == null) {
                            buffer = renderer.getBuffer(Sheets.translucentCullBlockSheet());
                        }
                        int glow = data.calculateGlowLight(MekanismRenderer.FULL_SKY_LIGHT);
                        matrix.pushPose();
                        matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
                        Model3D gasModel = ModelRenderer.getModel(data, 1);
                        MekanismRenderer.renderObject(gasModel, matrix, buffer, data.getColorARGB(multiblock.prevSteamScale), glow, overlayLight,
                              getFaceDisplay(data, gasModel));
                        matrix.popPose();
                    }
                }
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMOELECTRIC_BOILER;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityBoilerCasing tile) {
        if (tile.isMaster()) {
            BoilerMultiblockData multiblock = tile.getMultiblock();
            return multiblock.isFormed() && multiblock.renderLocation != null && multiblock.upperRenderLocation != null;
        }
        return false;
    }
}