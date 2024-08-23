package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class MekanismTileEntityRenderer<TILE extends BlockEntity> implements BlockEntityRenderer<TILE> {

    protected final BlockEntityRendererProvider.Context context;

    protected MekanismTileEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public int getViewDistance() {
        //Override and change the default range for TERs for mekanism tiles to the value defined in the config
        return MekanismConfig.client.berRange.get();
    }

    protected boolean isTickingNormally(TILE tile) {
        return !Minecraft.getInstance().isPaused() && MekanismUtils.isTickingNormally(tile.getLevel());
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

    protected void endIfNeeded(MultiBufferSource renderer, @Nullable RenderType renderType) {
        //If we are not on fabulous, and we are a multi buffer source, then we manually end the render type to ensure it can render
        // behind translucent things properly. If we are on fabulous we can skip this as it will be ended at the proper time
        //Note: Ideally we wouldn't have to do this so that if two TERs happen to render consecutively with the same render type
        // then they can batch themselves properly together, but such is the limitations of MC's rendering system
        if (!Minecraft.useShaderTransparency() && renderer instanceof MultiBufferSource.BufferSource source) {
            if (renderType == null) {
                source.endLastBatch();
            } else {
                source.endBatch(renderType);
            }
        }
    }

    protected abstract String getProfilerSection();

    protected Camera getCamera() {
        return context.getBlockEntityRenderDispatcher().camera;
    }

    protected final boolean isInsideBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return isInsideBounds(getCamera(), minX, minY, minZ, maxX, maxY, maxZ);
    }

    protected final boolean isInsideBounds(Camera camera, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        Vec3 projectedView = camera.getPosition();
        return minX <= projectedView.x && projectedView.x <= maxX &&
               minY <= projectedView.y && projectedView.y <= maxY &&
               minZ <= projectedView.z && projectedView.z <= maxZ;
    }

    protected final FaceDisplay getFaceDisplay(Camera camera, RenderData data, Model3D model) {
        return isInsideBounds(camera, data.location.getX(), data.location.getY(), data.location.getZ(),
              data.location.getX() + data.length, data.location.getY() + ModelRenderer.getActualHeight(model), data.location.getZ() + data.width)
               ? FaceDisplay.BACK : FaceDisplay.FRONT;
    }

    protected void renderObject(RenderData data, Set<ValveData> valves, BlockPos rendererPos, @NotNull PoseStack matrix, VertexConsumer buffer, int overlay, float scale) {
        Model3D model = ModelRenderer.getModel(data, scale);
        int glow = renderObject(data, rendererPos, model, matrix, buffer, overlay, scale);
        if (data instanceof FluidRenderData fluidRenderData && !valves.isEmpty()) {
            //Use the full multiblock's render data unlike getFaceDisplay which gets the current height for calculating if it is inside
            //If we are in the multiblock, render both faces of the valves as we may be "inside" of them or inside and outside them
            // if we aren't in the multiblock though we can just get away with only rendering the front faces
            FaceDisplay faceDisplay = isInsideBounds(data.location.getX(), data.location.getY(), data.location.getZ(), data.location.getX() + data.length,
                  data.location.getY() + data.height, data.location.getZ() + data.width) ? FaceDisplay.BOTH : FaceDisplay.FRONT;
            MekanismRenderer.renderValves(matrix, buffer, valves, fluidRenderData, model.maxY - model.minY, rendererPos, glow, overlay, faceDisplay, getCamera());
        }
    }

    protected int renderObject(RenderData data, BlockPos rendererPos, @NotNull PoseStack matrix, VertexConsumer buffer, int overlay, float scale) {
        return renderObject(data, rendererPos, ModelRenderer.getModel(data, scale), matrix, buffer, overlay, scale);
    }

    protected int renderObject(RenderData data, BlockPos rendererPos, Model3D object, @NotNull PoseStack matrix, VertexConsumer buffer, int overlay, float scale) {
        int glow = data.calculateGlowLight(LightTexture.FULL_SKY);
        Camera camera = getCamera();
        matrix.pushPose();
        matrix.translate(data.location.getX() - rendererPos.getX(), data.location.getY() - rendererPos.getY(), data.location.getZ() - rendererPos.getZ());
        MekanismRenderer.renderObject(object, matrix, buffer, data.getColorARGB(scale), glow, overlay, getFaceDisplay(camera, data, object), camera, data.location);
        matrix.popPose();
        return glow;
    }
}