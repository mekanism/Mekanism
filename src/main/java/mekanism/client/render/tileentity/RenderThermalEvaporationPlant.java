package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationBlock;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class RenderThermalEvaporationPlant extends MekanismTileEntityRenderer<TileEntityThermalEvaporationBlock> {

    public RenderThermalEvaporationPlant(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityThermalEvaporationBlock tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        if (tile.isMaster) {
            EvaporationMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed() && multiblock.renderLocation != null && !multiblock.inputTank.isEmpty()) {
                FluidRenderData data = new FluidRenderData(multiblock.inputTank.getFluid());
                data.location = multiblock.renderLocation.add(1, 0, 1);
                data.height = multiblock.height() - 2;
                data.length = 2;
                data.width = 2;
                matrix.push();
                BlockPos pos = tile.getPos();
                int glow = data.calculateGlowLight(MekanismRenderer.FULL_SKY_LIGHT);
                matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
                IVertexBuilder buffer = renderer.getBuffer(Atlases.getTranslucentCullBlockType());
                Model3D model = ModelRenderer.getModel(data, Math.min(1, multiblock.prevScale));
                MekanismRenderer.renderObject(model, matrix, buffer, data.getColorARGB(multiblock.prevScale), glow, overlayLight, getFaceDisplay(data, model));
                matrix.pop();
                MekanismRenderer.renderValves(matrix, buffer, multiblock.valves, data, pos, glow, overlayLight, isInsideMultiblock(data));
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMAL_EVAPORATION_CONTROLLER;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityThermalEvaporationBlock tile) {
        if (tile.isMaster) {
            EvaporationMultiblockData multiblock = tile.getMultiblock();
            return multiblock.isFormed() && !multiblock.inputTank.isEmpty() && multiblock.renderLocation != null;
        }
        return false;
    }
}