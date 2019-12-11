package mekanism.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import javax.annotation.Nonnull;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.FluidRenderer.ValveRenderData;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.MekanismFluids;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class RenderThermoelectricBoiler extends TileEntityRenderer<TileEntityBoilerCasing> {

    @Nonnull
    private static FluidStack STEAM = FluidStack.EMPTY;
    @Nonnull
    private static final FluidStack WATER = new FluidStack(Fluids.WATER, 1);

    @Override
    public void render(TileEntityBoilerCasing tile, double x, double y, double z, float partialTick, int destroyStage) {
        if (tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.renderLocation != null &&
            tile.structure.upperRenderLocation != null) {
            FluidStack waterStored = tile.structure.waterStored;
            boolean glChanged = false;
            if (waterStored.getAmount() > 0) {
                RenderData data = new RenderData();
                data.location = tile.structure.renderLocation;
                data.height = tile.structure.upperRenderLocation.y - 1 - tile.structure.renderLocation.y;
                data.length = tile.structure.volLength;
                data.width = tile.structure.volWidth;
                data.fluidType = WATER;

                if (data.height >= 1 && waterStored.getFluid() != Fluids.EMPTY) {
                    bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                    GlStateManager.pushMatrix();
                    glChanged = makeGLChanges(glChanged);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(waterStored);
                    MekanismRenderer.color(waterStored, (float) waterStored.getAmount() / (float) tile.clientWaterCapacity);
                    if (waterStored.getFluid().getAttributes().isGaseous(waterStored)) {
                        FluidRenderer.getTankDisplay(data).render();
                    } else {
                        FluidRenderer.getTankDisplay(data, tile.prevWaterScale).render();
                    }
                    MekanismRenderer.resetColor();
                    MekanismRenderer.disableGlow(glowInfo);
                    GlStateManager.popMatrix();

                    for (ValveData valveData : tile.valveViewing) {
                        GlStateManager.pushMatrix();
                        FluidRenderer.translateToOrigin(valveData.location);
                        GlowInfo valveGlowInfo = MekanismRenderer.enableGlow(waterStored);
                        FluidRenderer.getValveDisplay(ValveRenderData.get(data, valveData)).render();
                        MekanismRenderer.disableGlow(valveGlowInfo);
                        GlStateManager.popMatrix();
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

                if (data.height >= 1 && tile.structure.steamStored.getFluid() != Fluids.EMPTY) {
                    bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                    GlStateManager.pushMatrix();
                    glChanged = makeGLChanges(glChanged);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(tile.structure.steamStored);

                    DisplayInteger display = FluidRenderer.getTankDisplay(data);
                    MekanismRenderer.color(tile.structure.steamStored, (float) tile.structure.steamStored.getAmount() / (float) tile.clientSteamCapacity);
                    display.render();
                    MekanismRenderer.resetColor();
                    MekanismRenderer.disableGlow(glowInfo);
                    GlStateManager.popMatrix();
                }
            }
            if (glChanged) {
                setLightmapDisabled(false);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.disableCull();
            }
        }
    }

    private boolean makeGLChanges(boolean glChanged) {
        if (!glChanged) {
            GlStateManager.enableCull();
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            setLightmapDisabled(true);
        }
        return true;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityBoilerCasing tile) {
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.renderLocation != null && tile.structure.upperRenderLocation != null;
    }
}