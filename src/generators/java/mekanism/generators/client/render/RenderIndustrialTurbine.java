package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.api.annotations.NothingNullByDefault;
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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderIndustrialTurbine extends MekanismTileEntityRenderer<TileEntityTurbineCasing> {

    public RenderIndustrialTurbine(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityTurbineCasing tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        TurbineMultiblockData multiblock = tile.getMultiblock();
        BlockPos pos = tile.getBlockPos();
        BlockPos complexPos = multiblock.complex;
        VertexConsumer buffer = RenderTurbineRotor.INSTANCE.model.getBuffer(renderer);
        profiler.push(GeneratorsProfilerConstants.TURBINE_ROTOR);
        while (true) {
            complexPos = complexPos.below();
            TileEntityTurbineRotor rotor = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, tile.getLevel(), complexPos);
            if (rotor == null) {
                break;
            }
            matrix.pushPose();
            matrix.translate(complexPos.getX() - pos.getX(), complexPos.getY() - pos.getY(), complexPos.getZ() - pos.getZ());
            RenderTurbineRotor.INSTANCE.render(rotor, matrix, buffer, LightTexture.FULL_SKY, overlayLight);
            matrix.popPose();
        }
        profiler.pop();
        if (!multiblock.gasTank.isEmpty() && multiblock.length() > 0) {
            int height = multiblock.lowerVolume / (multiblock.length() * multiblock.width());
            if (height >= 1) {
                GasRenderData data = new GasRenderData(multiblock.gasTank.getStack());
                data.location = multiblock.renderLocation;
                data.height = height;
                data.length = multiblock.length();
                data.width = multiblock.width();
                int glow = data.calculateGlowLight(LightTexture.FULL_SKY);
                matrix.pushPose();
                matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
                Model3D gasModel = ModelRenderer.getModel(data, 1);
                MekanismRenderer.renderObject(gasModel, matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()),
                      data.getColorARGB(multiblock.prevSteamScale), glow, overlayLight, getFaceDisplay(data, gasModel));
                matrix.popPose();
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.INDUSTRIAL_TURBINE;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityTurbineCasing tile) {
        return true;
    }

    @Override
    public boolean shouldRender(TileEntityTurbineCasing tile, Vec3 camera) {
        if (tile.isMaster()) {
            TurbineMultiblockData multiblock = tile.getMultiblock();
            return multiblock.isFormed() && multiblock.complex != null && multiblock.renderLocation != null && super.shouldRender(tile, camera);
        }
        return false;
    }
}