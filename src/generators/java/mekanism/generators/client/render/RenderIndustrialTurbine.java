package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.ChemicalRenderData.GasRenderData;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.Atlases;
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
        if (tile.isMaster) {
            TurbineMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed() && multiblock.complex != null && multiblock.renderLocation != null) {
                BlockPos pos = tile.getPos();
                BlockPos complexPos = multiblock.complex;
                IVertexBuilder buffer = RenderTurbineRotor.INSTANCE.model.getBuffer(renderer);
                profiler.startSection(GeneratorsProfilerConstants.TURBINE_ROTOR);
                while (true) {
                    complexPos = complexPos.down();
                    TileEntityTurbineRotor rotor = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, tile.getWorld(), complexPos);
                    if (rotor == null) {
                        break;
                    }
                    matrix.push();
                    matrix.translate(complexPos.getX() - pos.getX(), complexPos.getY() - pos.getY(), complexPos.getZ() - pos.getZ());
                    RenderTurbineRotor.INSTANCE.render(rotor, matrix, buffer, MekanismRenderer.FULL_SKY_LIGHT, overlayLight);
                    matrix.pop();
                }
                profiler.endSection();
                if (!multiblock.gasTank.isEmpty() && multiblock.length() > 0) {
                    int height = multiblock.lowerVolume / (multiblock.length() * multiblock.width());
                    if (height >= 1) {
                        GasRenderData data = new GasRenderData(multiblock.gasTank.getStack());
                        data.location = multiblock.renderLocation;
                        data.height = height;
                        data.length = multiblock.length();
                        data.width = multiblock.width();
                        int glow = data.calculateGlowLight(MekanismRenderer.FULL_SKY_LIGHT);
                        matrix.push();
                        matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
                        Model3D gasModel = ModelRenderer.getModel(data, 1);
                        MekanismRenderer.renderObject(gasModel, matrix, renderer.getBuffer(Atlases.getTranslucentCullBlockType()),
                              data.getColorARGB(multiblock.prevSteamScale), glow, overlayLight, getFaceDisplay(data, gasModel));
                        matrix.pop();
                    }
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
        if (tile.isMaster) {
            TurbineMultiblockData multiblock = tile.getMultiblock();
            return multiblock.isFormed() && multiblock.complex != null && multiblock.renderLocation != null;
        }
        return false;
    }
}