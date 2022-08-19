package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.FluidRenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderThermalEvaporationPlant extends MekanismTileEntityRenderer<TileEntityThermalEvaporationController> {

    public RenderThermalEvaporationPlant(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityThermalEvaporationController tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        EvaporationMultiblockData multiblock = tile.getMultiblock();
        FluidRenderData data = new FluidRenderData(multiblock.inputTank.getFluid());
        data.location = multiblock.renderLocation.offset(1, 0, 1);
        data.height = multiblock.height() - 2;
        data.length = 2;
        data.width = 2;
        matrix.pushPose();
        BlockPos pos = tile.getBlockPos();
        int glow = data.calculateGlowLight(LightTexture.FULL_SKY);
        matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
        VertexConsumer buffer = renderer.getBuffer(Sheets.translucentCullBlockSheet());
        Model3D model = ModelRenderer.getModel(data, Math.min(1, multiblock.prevScale));
        MekanismRenderer.renderObject(model, matrix, buffer, data.getColorARGB(multiblock.prevScale), glow, overlayLight, getFaceDisplay(data, model));
        matrix.popPose();
        MekanismRenderer.renderValves(matrix, buffer, multiblock.valves, data, pos, glow, overlayLight, isInsideMultiblock(data));
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMAL_EVAPORATION_CONTROLLER;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityThermalEvaporationController tile) {
        return true;
    }

    @Override
    public boolean shouldRender(TileEntityThermalEvaporationController tile, Vec3 camera) {
        if (tile.isMaster()) {
            EvaporationMultiblockData multiblock = tile.getMultiblock();
            return multiblock.isFormed() && !multiblock.inputTank.isEmpty() && multiblock.renderLocation != null && super.shouldRender(tile, camera);
        }
        return false;
    }
}