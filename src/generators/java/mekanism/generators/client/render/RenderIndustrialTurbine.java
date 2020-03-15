package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.GasRenderData;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
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
        if (tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.complex != null && tile.structure.renderLocation != null) {
            RenderTurbineRotor.internalRender = true;
            BlockPos pos = tile.getPos();
            BlockPos complexPos = tile.structure.complex.getPos();
            while (true) {
                complexPos = complexPos.down();
                TileEntityTurbineRotor rotor = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, tile.getWorld(), complexPos);
                if (rotor == null) {
                    break;
                }
                matrix.push();
                matrix.translate(complexPos.getX() - pos.getX(), complexPos.getY() - pos.getY(), complexPos.getZ() - pos.getZ());
                //TODO: Batch all the rotor rendering into a single render type rendering
                renderDispatcher.renderItem(rotor, matrix, renderer, MekanismRenderer.FULL_LIGHT, overlayLight);
                matrix.pop();
            }
            RenderTurbineRotor.internalRender = false;
            if (!tile.structure.gasTank.isEmpty() && tile.structure.volLength > 0) {
                GasRenderData data = new GasRenderData();
                data.height = tile.structure.lowerVolume / (tile.structure.volLength * tile.structure.volWidth);
                if (data.height >= 1) {
                    data.location = tile.structure.renderLocation;
                    data.length = tile.structure.volLength;
                    data.width = tile.structure.volWidth;
                    data.gasType = tile.structure.gasTank.getStack();
                    matrix.push();
                    matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                    Model3D gasModel = ModelRenderer.getModel(data, 1);
                    MekanismRenderer.renderObject(gasModel, matrix, renderer.getBuffer(MekanismRenderType.resizableCuboid()), data.getColorARGB(tile.prevSteamScale),
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
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.complex != null && tile.structure.renderLocation != null;
    }
}