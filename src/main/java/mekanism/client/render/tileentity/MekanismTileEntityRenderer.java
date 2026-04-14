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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault//TODO - 1.21.11: Test all our renderers, and figure out if/how to get profiling per type working again
public abstract class MekanismTileEntityRenderer<TILE extends BlockEntity, STATE extends BlockEntityRenderState> implements BlockEntityRenderer<TILE, STATE> {

    protected final BlockEntityRendererProvider.Context context;

    //TODO - 1.21.11: do we want to be passing context all the way up, or just grab what we need where we need it? I think probably the latter
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

    protected abstract String getProfilerSection();

    protected final boolean isInsideBounds(Vec3 camera, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return minX <= camera.x && camera.x <= maxX &&
               minY <= camera.y && camera.y <= maxY &&
               minZ <= camera.z && camera.z <= maxZ;
    }

    protected final FaceDisplay getFaceDisplay(Vec3 camPos, RenderData data, Model3D model) {
        return isInsideBounds(camPos, data.location.getX(), data.location.getY(), data.location.getZ(),
              data.location.getX() + data.length, data.location.getY() + ModelRenderer.getActualHeight(model), data.location.getZ() + data.width)
               ? FaceDisplay.BACK : FaceDisplay.FRONT;
    }

    protected void renderObject(Vec3 camPos, RenderData data, Set<ValveData> valves, BlockPos rendererPos, PoseStack matrix, VertexConsumer buffer, int overlay, float scale) {
        Model3D model = ModelRenderer.getModel(data, scale);
        int glow = renderObject(camPos, data, rendererPos, model, matrix, buffer, overlay, scale);
        if (data instanceof FluidRenderData fluidRenderData && !valves.isEmpty()) {
            //Use the full multiblock's render data unlike getFaceDisplay which gets the current height for calculating if it is inside
            //If we are in the multiblock, render both faces of the valves as we may be "inside" of them or inside and outside them
            // if we aren't in the multiblock though we can just get away with only rendering the front faces
            FaceDisplay faceDisplay = isInsideBounds(camPos, data.location.getX(), data.location.getY(), data.location.getZ(), data.location.getX() + data.length,
                  data.location.getY() + data.height, data.location.getZ() + data.width) ? FaceDisplay.BOTH : FaceDisplay.FRONT;
            MekanismRenderer.renderValves(matrix, buffer, valves, fluidRenderData, model.maxY - model.minY, rendererPos, glow, overlay, faceDisplay, camPos);
        }
    }

    protected int renderObject(Vec3 camPos, RenderData data, BlockPos rendererPos, PoseStack matrix, VertexConsumer buffer, int overlay, float scale) {
        return renderObject(camPos, data, rendererPos, ModelRenderer.getModel(data, scale), matrix, buffer, overlay, scale);
    }

    //TODO - 1.21.11: Should we no-op all the cases of scale == 0
    protected int renderObject(Vec3 camPos, RenderData data, BlockPos rendererPos, Model3D object, PoseStack matrix, VertexConsumer buffer, int overlay, float scale) {
        int glow = data.calculateGlowLight(LightCoordsUtil.FULL_SKY);
        matrix.pushPose();
        matrix.translate(data.location.getX() - rendererPos.getX(), data.location.getY() - rendererPos.getY(), data.location.getZ() - rendererPos.getZ());
        MekanismRenderer.renderObject(object, matrix, buffer, data.getColorARGB(scale), glow, overlay, getFaceDisplay(camPos, data, object), camPos, data.location);
        matrix.popPose();
        return glow;
    }
}