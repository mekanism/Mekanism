package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.BooleanSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.data.RenderData;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3d;

@ParametersAreNonnullByDefault
public abstract class MekanismTileEntityRenderer<TILE extends TileEntity> extends TileEntityRenderer<TILE> {

    protected MekanismTileEntityRenderer(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(TILE tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight) {
        if (tile.getWorld() != null) {
            IProfiler profiler = tile.getWorld().getProfiler();
            profiler.startSection(getProfilerSection());
            render(tile, partialTick, matrix, renderer, light, overlayLight, profiler);
            profiler.endSection();
        }
    }

    protected abstract void render(TILE tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler);

    protected abstract String getProfilerSection();

    protected boolean isInsideBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        Vector3d projectedView = renderDispatcher.renderInfo.getProjectedView();
        return minX <= projectedView.x && projectedView.x <= maxX &&
               minY <= projectedView.y && projectedView.y <= maxY &&
               minZ <= projectedView.z && projectedView.z <= maxZ;
    }

    protected FaceDisplay getFaceDisplay(RenderData data, Model3D model) {
        return isInsideBounds(data.location.getX(), data.location.getY(), data.location.getZ(),
              data.location.getX() + data.length, data.location.getY() + ModelRenderer.getActualHeight(model), data.location.getZ() + data.width)
               ? FaceDisplay.BACK : FaceDisplay.FRONT;
    }

    protected BooleanSupplier isInsideMultiblock(RenderData data) {
        //Use the full multiblock's render data unlike getFaceDisplay which gets the current height
        return () -> isInsideBounds(data.location.getX(), data.location.getY(), data.location.getZ(),
              data.location.getX() + data.length, data.location.getY() + data.height, data.location.getZ() + data.width);
    }
}