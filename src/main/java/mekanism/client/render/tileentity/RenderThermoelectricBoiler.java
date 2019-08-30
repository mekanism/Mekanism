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
import mekanism.common.MekanismGases;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class RenderThermoelectricBoiler extends TileEntityRenderer<TileEntityBoilerCasing> {

    @Nonnull
    private static final FluidStack STEAM = new FluidStack(MekanismGases.STEAM.getFluid(), 1);
    @Nonnull
    private static final FluidStack WATER = new FluidStack(Fluids.WATER, 1);

    @Override
    public void render(TileEntityBoilerCasing tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        if (tileEntity.clientHasStructure && tileEntity.isRendering && tileEntity.structure != null && tileEntity.structure.renderLocation != null &&
            tileEntity.structure.upperRenderLocation != null) {
            FluidStack waterStored = tileEntity.structure.waterStored;
            boolean glChanged = false;
            if (waterStored.getAmount() > 0) {
                RenderData data = new RenderData();
                data.location = tileEntity.structure.renderLocation;
                data.height = tileEntity.structure.upperRenderLocation.y - 1 - tileEntity.structure.renderLocation.y;
                data.length = tileEntity.structure.volLength;
                data.width = tileEntity.structure.volWidth;
                data.fluidType = WATER;

                if (data.height >= 1 && waterStored.getFluid() != Fluids.EMPTY) {
                    bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                    GlStateManager.pushMatrix();
                    glChanged = makeGLChanges(glChanged);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(waterStored);
                    MekanismRenderer.color(waterStored, (float) waterStored.getAmount() / (float) tileEntity.clientWaterCapacity);
                    if (waterStored.getFluid().getAttributes().isGaseous(waterStored)) {
                        FluidRenderer.getTankDisplay(data).render();
                    } else {
                        FluidRenderer.getTankDisplay(data, tileEntity.prevWaterScale).render();
                    }
                    MekanismRenderer.resetColor();
                    MekanismRenderer.disableGlow(glowInfo);
                    GlStateManager.popMatrix();

                    for (ValveData valveData : tileEntity.valveViewing) {
                        GlStateManager.pushMatrix();
                        FluidRenderer.translateToOrigin(valveData.location);
                        GlowInfo valveGlowInfo = MekanismRenderer.enableGlow(waterStored);
                        FluidRenderer.getValveDisplay(ValveRenderData.get(data, valveData)).render();
                        MekanismRenderer.disableGlow(valveGlowInfo);
                        GlStateManager.popMatrix();
                    }
                }
            }

            if (tileEntity.structure.steamStored.getAmount() > 0) {
                RenderData data = new RenderData();
                data.location = tileEntity.structure.upperRenderLocation;
                data.height = tileEntity.structure.renderLocation.y + tileEntity.structure.volHeight - 2 - tileEntity.structure.upperRenderLocation.y;
                data.length = tileEntity.structure.volLength;
                data.width = tileEntity.structure.volWidth;
                data.fluidType = STEAM;

                if (data.height >= 1 && tileEntity.structure.steamStored.getFluid() != Fluids.EMPTY) {
                    bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                    GlStateManager.pushMatrix();
                    glChanged = makeGLChanges(glChanged);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(tileEntity.structure.steamStored);

                    DisplayInteger display = FluidRenderer.getTankDisplay(data);
                    MekanismRenderer.color(tileEntity.structure.steamStored, (float) tileEntity.structure.steamStored.getAmount() / (float) tileEntity.clientSteamCapacity);
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
}