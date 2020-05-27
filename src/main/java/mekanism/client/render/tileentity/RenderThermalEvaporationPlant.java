package mekanism.client.render.tileentity;

import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationBlock;
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
        if (tile.isMaster && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null && !tile.getMultiblock().inputTank.isEmpty()) {
            FluidRenderData data = new FluidRenderData(tile.getMultiblock().inputTank.getFluid());
            data.location = new Coord4D(tile.getMultiblock().renderLocation.add(1, 0, 1), tile.getWorld());
            data.height = tile.getMultiblock().height() - 2;
            data.length = 2;
            data.width = 2;
            matrix.push();
            BlockPos pos = tile.getPos();
            int glow = data.calculateGlowLight(light);
            matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
            IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
            MekanismRenderer.renderObject(ModelRenderer.getModel(data, Math.min(1, tile.getMultiblock().prevScale)), matrix, buffer,
                  data.getColorARGB(tile.getMultiblock().prevScale), glow);
            matrix.pop();
            MekanismRenderer.renderValves(matrix, buffer, tile.getMultiblock().valves, data, pos, glow);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMAL_EVAPORATION_CONTROLLER;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityThermalEvaporationBlock tile) {
        return tile.isMaster && tile.getMultiblock().isFormed() && !tile.getMultiblock().inputTank.isEmpty() &&
              tile.getMultiblock().renderLocation != null;
    }
}