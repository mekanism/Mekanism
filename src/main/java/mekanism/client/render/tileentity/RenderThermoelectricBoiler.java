package mekanism.client.render.tileentity;

import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.GasRenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class RenderThermoelectricBoiler extends MekanismTileEntityRenderer<TileEntityBoilerCasing> {

    public RenderThermoelectricBoiler(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityBoilerCasing tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.isRendering && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null && tile.getMultiblock().upperRenderLocation != null) {
            BlockPos pos = tile.getPos();
            IVertexBuilder buffer = null;
            if (!tile.getMultiblock().waterTank.isEmpty()) {
                FluidRenderData data = new FluidRenderData();
                data.height = tile.getMultiblock().upperRenderLocation.y - 1 - tile.getMultiblock().renderLocation.y;
                if (data.height >= 1) {
                    data.location = tile.getMultiblock().renderLocation;
                    data.length = tile.getMultiblock().volLength;
                    data.width = tile.getMultiblock().volWidth;
                    data.fluidType = tile.getMultiblock().waterTank.getFluid();
                    int glow = data.calculateGlowLight(light);
                    matrix.push();
                    matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                    buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
                    MekanismRenderer.renderObject(ModelRenderer.getModel(data, tile.getMultiblock().prevWaterScale), matrix, buffer, data.getColorARGB(tile.getMultiblock().prevWaterScale), glow);
                    matrix.pop();

                    MekanismRenderer.renderValves(matrix, buffer, tile.getMultiblock().valves, data, pos, glow);
                }
            }
            if (!tile.getMultiblock().steamTank.isEmpty()) {
                GasRenderData data = new GasRenderData();
                data.height = tile.getMultiblock().renderLocation.y + tile.getMultiblock().volHeight - 2 - tile.getMultiblock().upperRenderLocation.y;
                if (data.height >= 1) {
                    data.location = tile.getMultiblock().upperRenderLocation;
                    data.length = tile.getMultiblock().volLength;
                    data.width = tile.getMultiblock().volWidth;
                    data.gasType = tile.getMultiblock().steamTank.getStack();
                    if (buffer == null) {
                        buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
                    }
                    matrix.push();
                    matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                    Model3D gasModel = ModelRenderer.getModel(data, 1);
                    MekanismRenderer.renderObject(gasModel, matrix, buffer, data.getColorARGB(tile.getMultiblock().prevSteamScale), data.calculateGlowLight(light));
                    matrix.pop();
                }
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMOELECTRIC_BOILER;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityBoilerCasing tile) {
        return tile.isRendering && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null && tile.getMultiblock().upperRenderLocation != null;
    }
}