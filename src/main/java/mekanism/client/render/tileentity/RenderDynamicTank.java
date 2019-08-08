package mekanism.client.render.tileentity;

import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.FluidRenderer.ValveRenderData;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityDynamicTank;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderDynamicTank extends TileEntityRenderer<TileEntityDynamicTank> {

    @Override
    public void render(TileEntityDynamicTank tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        if (tileEntity.clientHasStructure && tileEntity.isRendering && tileEntity.structure != null && tileEntity.structure.fluidStored != null &&
            tileEntity.structure.fluidStored.amount != 0) {
            RenderData data = new RenderData();
            data.location = tileEntity.structure.renderLocation;
            data.height = tileEntity.structure.volHeight - 2;
            data.length = tileEntity.structure.volLength;
            data.width = tileEntity.structure.volWidth;
            data.fluidType = tileEntity.structure.fluidStored;

            if (data.location != null && data.height >= 1) {
                bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                GlStateManager.pushMatrix();
                GlStateManager.enableCull();
                GlStateManager.enableBlend();
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                FluidRenderer.translateToOrigin(data.location);
                GlowInfo glowInfo = MekanismRenderer.enableGlow(data.fluidType);
                MekanismRenderer.color(data.fluidType, (float) data.fluidType.amount / (float) tileEntity.clientCapacity);
                if (data.fluidType.getFluid().isGaseous(data.fluidType)) {
                    FluidRenderer.getTankDisplay(data).render();
                } else {
                    FluidRenderer.getTankDisplay(data, tileEntity.prevScale).render();
                }

                MekanismRenderer.resetColor();
                MekanismRenderer.disableGlow(glowInfo);
                GlStateManager.popMatrix();

                for (ValveData valveData : tileEntity.valveViewing) {
                    GlStateManager.pushMatrix();
                    FluidRenderer.translateToOrigin(valveData.location);
                    GlowInfo valveGlowInfo = MekanismRenderer.enableGlow(data.fluidType);
                    MekanismRenderer.color(data.fluidType);
                    FluidRenderer.getValveDisplay(ValveRenderData.get(data, valveData)).render();
                    MekanismRenderer.disableGlow(valveGlowInfo);
                    GlStateManager.popMatrix();
                }
                MekanismRenderer.resetColor();
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.disableCull();
            }
        }
    }
}