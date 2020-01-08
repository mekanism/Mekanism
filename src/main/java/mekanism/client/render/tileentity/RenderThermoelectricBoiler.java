package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.FluidRenderer.ValveRenderData;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public class RenderThermoelectricBoiler extends TileEntityRenderer<TileEntityBoilerCasing> {

    @Nonnull
    private static FluidStack STEAM = FluidStack.EMPTY;
    @Nonnull
    private static final FluidStack WATER = new FluidStack(Fluids.WATER, 1);

    public RenderThermoelectricBoiler(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityBoilerCasing tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int overlayLight) {
        if (tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.renderLocation != null &&
            tile.structure.upperRenderLocation != null) {
            FluidStack waterStored = tile.structure.waterStored;
            BlockPos pos = tile.getPos();
            if (waterStored.getAmount() > 0) {
                RenderData data = new RenderData();
                data.location = tile.structure.renderLocation;
                data.height = tile.structure.upperRenderLocation.y - 1 - tile.structure.renderLocation.y;
                data.length = tile.structure.volLength;
                data.width = tile.structure.volWidth;
                data.fluidType = WATER;

                if (data.height >= 1 && !waterStored.isEmpty()) {
                    matrix.func_227860_a_();
                    matrix.func_227861_a_(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(waterStored);
                    Model3D fluidModel = FluidRenderer.getFluidModel(data, tile.prevWaterScale);
                    MekanismRenderer.renderObject(fluidModel, matrix, renderer, MekanismRenderType.renderFluidState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                          MekanismRenderer.getColorARGB(data.fluidType, (float) waterStored.getAmount() / (float) tile.clientWaterCapacity));
                    MekanismRenderer.disableGlow(glowInfo);
                    matrix.func_227865_b_();

                    for (ValveData valveData : tile.valveViewing) {
                        matrix.func_227860_a_();
                        matrix.func_227861_a_(valveData.location.x - pos.getX(), valveData.location.y - pos.getY(), valveData.location.z - pos.getZ());
                        GlowInfo valveGlowInfo = MekanismRenderer.enableGlow(waterStored);
                        Model3D valveModel = FluidRenderer.getValveModel(ValveRenderData.get(data, valveData));
                        MekanismRenderer.renderObject(valveModel, matrix, renderer, MekanismRenderType.renderFluidState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                              MekanismRenderer.getColorARGB(data.fluidType));
                        MekanismRenderer.disableGlow(valveGlowInfo);
                        matrix.func_227865_b_();
                    }
                }
            }

            if (tile.structure.steamStored.getAmount() > 0) {
                if (STEAM.isEmpty()) {
                    STEAM = MekanismFluids.STEAM.getFluidStack(1);
                }
                RenderData data = new RenderData();
                data.location = tile.structure.upperRenderLocation;
                data.height = tile.structure.renderLocation.y + tile.structure.volHeight - 2 - tile.structure.upperRenderLocation.y;
                data.length = tile.structure.volLength;
                data.width = tile.structure.volWidth;
                data.fluidType = STEAM;
                if (data.height >= 1 && !tile.structure.steamStored.isEmpty()) {
                    matrix.func_227860_a_();
                    matrix.func_227861_a_(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(tile.structure.steamStored);
                    Model3D fluidModel = FluidRenderer.getFluidModel(data, 1);
                    MekanismRenderer.renderObject(fluidModel, matrix, renderer, MekanismRenderType.renderFluidState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                          MekanismRenderer.getColorARGB(tile.structure.steamStored, (float) tile.structure.steamStored.getAmount() / (float) tile.clientSteamCapacity));
                    MekanismRenderer.disableGlow(glowInfo);
                    matrix.func_227865_b_();
                }
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityBoilerCasing tile) {
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.renderLocation != null && tile.structure.upperRenderLocation != null;
    }
}