package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.BooleanSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.data.RenderData;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

@ParametersAreNonnullByDefault
public abstract class MekanismTileEntityRenderer<TILE extends BlockEntity> implements BlockEntityRenderer<TILE> {

    protected final BlockEntityRendererProvider.Context context;

    protected MekanismTileEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public int getViewDistance() {
        //Override and change the default range for TERs for mekanism tiles to the value defined in the config
        return MekanismConfig.client.terRange.get();
    }

    @Override
    public void render(TILE tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight) {
        if (tile.getLevel() != null) {
            ProfilerFiller profiler = tile.getLevel().getProfiler();
            profiler.push(getProfilerSection());
            render(tile, partialTick, matrix, renderer, light, overlayLight, profiler);
            profiler.pop();
        }
    }

    protected abstract void render(TILE tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler);

    protected abstract String getProfilerSection();

    protected boolean isInsideBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        Vec3 projectedView = context.getBlockEntityRenderDispatcher().camera.getPosition();
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