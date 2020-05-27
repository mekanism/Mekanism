package mekanism.client.render.tileentity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.GasRenderData;
import mekanism.client.render.data.InfusionRenderData;
import mekanism.client.render.data.PigmentRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.client.render.data.SlurryRenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
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
        if (tile.isMaster && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null) {
            RenderData data = getRenderData(tile.getMultiblock());
            if (data != null) {
                data.location = new Coord4D(tile.getMultiblock().renderLocation, tile.getWorld());
                data.height = tile.getMultiblock().height() - 2;
                data.length = tile.getMultiblock().length();
                data.width = tile.getMultiblock().width();
                matrix.push();

                IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
                BlockPos pos = tile.getPos();
                matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                int glow = data.calculateGlowLight(light);
                Model3D model = ModelRenderer.getModel(data, tile.getMultiblock().prevScale);
                MekanismRenderer.renderObject(model, matrix, buffer, data.getColorARGB(tile.getMultiblock().prevScale), glow);
                matrix.pop();
                if (data instanceof FluidRenderData) {
                    MekanismRenderer.renderValves(matrix, buffer, tile.getMultiblock().valves, (FluidRenderData) data, pos, glow);
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
        return tile.isMaster && tile.getMultiblock().isFormed() && !tile.getMultiblock().isEmpty() && tile.getMultiblock().renderLocation != null;
    }
}