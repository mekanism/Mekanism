package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class RenderThermalEvaporationController extends MekanismTileEntityRenderer<TileEntityThermalEvaporationController> {

    public RenderThermalEvaporationController(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityThermalEvaporationController tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        if (tile.getActive() && tile.height - 2 >= 1 && !tile.inputTank.isEmpty()) {
            FluidRenderData data = new FluidRenderData();
            data.location = new Coord4D(tile.getRenderLocation(), tile.getWorld());
            data.height = tile.height - 2;
            data.length = 2;
            data.width = 2;
            data.fluidType = tile.inputTank.getFluid();
            matrix.push();
            BlockPos pos = tile.getPos();
            matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
            MekanismRenderer.renderObject(ModelRenderer.getModel(data, Math.min(1, tile.prevScale)), matrix, renderer.getBuffer(MekanismRenderType.resizableCuboid()),
                  data.getColorARGB(tile.prevScale), data.calculateGlowLight(light));
            matrix.pop();
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMAL_EVAPORATION_CONTROLLER;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityThermalEvaporationController tile) {
        return tile.getActive() && tile.height - 2 >= 1 && !tile.inputTank.isEmpty();
    }
}