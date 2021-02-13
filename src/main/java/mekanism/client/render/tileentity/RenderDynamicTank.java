package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.ChemicalRenderData.GasRenderData;
import mekanism.client.render.data.ChemicalRenderData.InfusionRenderData;
import mekanism.client.render.data.ChemicalRenderData.PigmentRenderData;
import mekanism.client.render.data.ChemicalRenderData.SlurryRenderData;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class RenderDynamicTank extends MekanismTileEntityRenderer<TileEntityDynamicTank> {

    public RenderDynamicTank(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityDynamicTank tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.isMaster) {
            TankMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed() && multiblock.renderLocation != null) {
                RenderData data = getRenderData(multiblock);
                if (data != null) {
                    data.location = multiblock.renderLocation;
                    data.height = multiblock.height() - 2;
                    data.length = multiblock.length();
                    data.width = multiblock.width();
                    matrix.push();

                    IVertexBuilder buffer = renderer.getBuffer(Atlases.getTranslucentCullBlockType());
                    BlockPos pos = tile.getPos();
                    matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
                    int glow = data.calculateGlowLight(MekanismRenderer.FULL_SKY_LIGHT);
                    Model3D model = ModelRenderer.getModel(data, multiblock.prevScale);
                    MekanismRenderer.renderObject(model, matrix, buffer, data.getColorARGB(multiblock.prevScale), glow, overlayLight, getFaceDisplay(data, model));
                    matrix.pop();
                    if (data instanceof FluidRenderData) {
                        MekanismRenderer.renderValves(matrix, buffer, multiblock.valves, (FluidRenderData) data, pos, glow, overlayLight, isInsideMultiblock(data));
                    }
                }
            }
        }
    }

    @Nullable
    private RenderData getRenderData(TankMultiblockData multiblock) {
        switch (multiblock.mergedTank.getCurrentType()) {
            case FLUID:
                return new FluidRenderData(multiblock.getFluidTank().getFluid());
            case GAS:
                return new GasRenderData(multiblock.getGasTank().getStack());
            case INFUSION:
                return new InfusionRenderData(multiblock.getInfusionTank().getStack());
            case PIGMENT:
                return new PigmentRenderData(multiblock.getPigmentTank().getStack());
            case SLURRY:
                return new SlurryRenderData(multiblock.getSlurryTank().getStack());
        }
        return null;
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.DYNAMIC_TANK;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityDynamicTank tile) {
        if (tile.isMaster) {
            TankMultiblockData multiblock = tile.getMultiblock();
            return multiblock.isFormed() && !multiblock.isEmpty() && multiblock.renderLocation != null;
        }
        return false;
    }
}