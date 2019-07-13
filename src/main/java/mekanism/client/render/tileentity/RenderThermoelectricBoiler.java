package mekanism.client.render.tileentity;

import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.FluidRenderer.ValveRenderData;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderThermoelectricBoiler extends TileEntitySpecialRenderer<TileEntityBoilerCasing> {

    private FluidStack STEAM = new FluidStack(FluidRegistry.getFluid("steam"), 1);
    private FluidStack WATER = new FluidStack(FluidRegistry.WATER, 1);

    @Override
    public void render(TileEntityBoilerCasing tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (tileEntity.clientHasStructure && tileEntity.isRendering && tileEntity.structure != null && tileEntity.structure.renderLocation != null &&
            tileEntity.structure.upperRenderLocation != null) {
            FluidStack waterStored = tileEntity.structure.waterStored;
            if (waterStored != null && waterStored.amount != 0) {
                RenderData data = new RenderData();
                data.location = tileEntity.structure.renderLocation;
                data.height = tileEntity.structure.upperRenderLocation.y - 1 - tileEntity.structure.renderLocation.y;
                data.length = tileEntity.structure.volLength;
                data.width = tileEntity.structure.volWidth;
                data.fluidType = WATER;

                if (data.height >= 1 && waterStored.getFluid() != null) {
                    bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableCull();
                    GlStateManager.enableBlend();
                    GlStateManager.disableLighting();
                    GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(waterStored);
                    MekanismRenderer.color(waterStored, (float) waterStored.amount / (float) tileEntity.clientWaterCapacity);
                    if (waterStored.getFluid().isGaseous(waterStored)) {
                        FluidRenderer.getTankDisplay(data).render();
                    } else {
                        FluidRenderer.getTankDisplay(data, tileEntity.prevWaterScale).render();
                    }
                    MekanismRenderer.resetColor();
                    MekanismRenderer.disableGlow(glowInfo);
                    GlStateManager.popMatrix();

                    for (ValveData valveData : tileEntity.valveViewing) {
                        GlStateManager.pushMatrix();
                        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                        FluidRenderer.translateToOrigin(valveData.location);
                        GlowInfo valveGlowInfo = MekanismRenderer.enableGlow(waterStored);
                        FluidRenderer.getValveDisplay(ValveRenderData.get(data, valveData)).render();
                        MekanismRenderer.disableGlow(valveGlowInfo);
                        GlStateManager.popMatrix();
                    }
                    GlStateManager.enableLighting();
                    GlStateManager.disableBlend();
                    GlStateManager.disableCull();
                }
            }

            if (tileEntity.structure.steamStored != null && tileEntity.structure.steamStored.amount > 0) {
                RenderData data = new RenderData();
                data.location = tileEntity.structure.upperRenderLocation;
                data.height = tileEntity.structure.renderLocation.y + tileEntity.structure.volHeight - 2 - tileEntity.structure.upperRenderLocation.y;
                data.length = tileEntity.structure.volLength;
                data.width = tileEntity.structure.volWidth;
                data.fluidType = STEAM;

                if (data.height >= 1 && tileEntity.structure.steamStored.getFluid() != null) {
                    bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableCull();
                    GlStateManager.enableBlend();
                    GlStateManager.disableLighting();
                    GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(tileEntity.structure.steamStored);

                    DisplayInteger display = FluidRenderer.getTankDisplay(data);
                    MekanismRenderer.color(tileEntity.structure.steamStored, (float) tileEntity.structure.steamStored.amount / (float) tileEntity.clientSteamCapacity);
                    display.render();
                    MekanismRenderer.resetColor();
                    MekanismRenderer.disableGlow(glowInfo);
                    GlStateManager.enableLighting();
                    GlStateManager.disableBlend();
                    GlStateManager.disableCull();
                    GlStateManager.popMatrix();
                }
            }
        }
    }
}