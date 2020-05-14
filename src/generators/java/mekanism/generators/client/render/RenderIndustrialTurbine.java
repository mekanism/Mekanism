package mekanism.generators.client.render;

import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.GasRenderData;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class RenderIndustrialTurbine extends MekanismTileEntityRenderer<TileEntityTurbineCasing> {

    public RenderIndustrialTurbine(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityTurbineCasing tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.isRendering && tile.getMultiblock().isFormed() && tile.getMultiblock().complex != null && tile.getMultiblock().renderLocation != null) {
            BlockPos pos = tile.getPos();
            BlockPos complexPos = tile.getMultiblock().complex.getPos();
            IVertexBuilder buffer = RenderTurbineRotor.INSTANCE.model.getBuffer(renderer);
            profiler.startSection(GeneratorsProfilerConstants.TURBINE_ROTOR);
            while (true) {
                complexPos = complexPos.down();
                TileEntityTurbineRotor rotor = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, tile.getWorld(), complexPos);
                if (rotor == null) {
                    break;
                }
                matrix.push();
                matrix.translate(complexPos.getX() - pos.getX(), complexPos.getY() - pos.getY(), complexPos.getZ() - pos.getZ());
                RenderTurbineRotor.INSTANCE.render(rotor, matrix, buffer, MekanismRenderer.FULL_LIGHT, overlayLight);
                matrix.pop();
            }
            profiler.endSection();
            if (!tile.getMultiblock().gasTank.isEmpty() && tile.getMultiblock().volLength > 0) {
                GasRenderData data = new GasRenderData();
                data.height = tile.getMultiblock().lowerVolume / (tile.getMultiblock().volLength * tile.getMultiblock().volWidth);
                if (data.height >= 1) {
                    data.location = tile.getMultiblock().renderLocation;
                    data.length = tile.getMultiblock().volLength;
                    data.width = tile.getMultiblock().volWidth;
                    data.gasType = tile.getMultiblock().gasTank.getStack();
                    matrix.push();
                    matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                    Model3D gasModel = ModelRenderer.getModel(data, 1);
                    MekanismRenderer.renderObject(gasModel, matrix, renderer.getBuffer(MekanismRenderType.resizableCuboid()), data.getColorARGB(tile.getMultiblock().prevSteamScale),
                          data.calculateGlowLight(light));
                    matrix.pop();
                }
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.INDUSTRIAL_TURBINE;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTurbineCasing tile) {
        return tile.isRendering && tile.getMultiblock().isFormed() && tile.getMultiblock().complex != null && tile.getMultiblock().renderLocation != null;
    }
}