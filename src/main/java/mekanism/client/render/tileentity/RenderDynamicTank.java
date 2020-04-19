package mekanism.client.render.tileentity;

import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityDynamicTank;
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
        if (tile.clientHasStructure && tile.isRendering && tile.structure != null && !tile.structure.fluidTank.isEmpty() && tile.structure.renderLocation != null
            && tile.structure.volHeight > 2) {
            FluidRenderData data = new FluidRenderData();
            data.location = tile.structure.renderLocation;
            data.height = tile.structure.volHeight - 2;
            data.length = tile.structure.volLength;
            data.width = tile.structure.volWidth;
            data.fluidType = tile.structure.fluidTank.getFluid();

            matrix.push();
            IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.resizableCuboid());
            BlockPos pos = tile.getPos();
            matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
            int glow = data.calculateGlowLight(light);
            Model3D fluidModel = ModelRenderer.getModel(data, tile.prevScale);
            MekanismRenderer.renderObject(fluidModel, matrix, buffer, data.getColorARGB(tile.prevScale), glow);
            matrix.pop();

            MekanismRenderer.renderValves(matrix, buffer, tile.structure.valves, data, pos, glow);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.DYNAMIC_TANK;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityDynamicTank tile) {
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && !tile.structure.fluidTank.isEmpty() && tile.structure.renderLocation != null
               && tile.structure.volHeight > 2;
    }
}