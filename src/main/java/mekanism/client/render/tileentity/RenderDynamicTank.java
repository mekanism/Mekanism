package mekanism.client.render.tileentity;

import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.FluidRenderer.ValveRenderData;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDynamicTank extends TileEntitySpecialRenderer<TileEntityDynamicTank> {

    @Override
    public void render(TileEntityDynamicTank tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (tileEntity.clientHasStructure && tileEntity.isRendering && tileEntity.structure != null && tileEntity.structure.fluidStored != null &&
            tileEntity.structure.fluidStored.amount != 0) {
            RenderData data = new RenderData();
            data.location = tileEntity.structure.renderLocation;
            data.height = tileEntity.structure.volHeight - 2;
            data.length = tileEntity.structure.volLength;
            data.width = tileEntity.structure.volWidth;
            data.fluidType = tileEntity.structure.fluidStored;

            if (data.location != null && data.height >= 1) {
                bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                MekanismRenderHelper renderHelper = FluidRenderer.initHelper();
                FluidRenderer.translateToOrigin(data.location);
                renderHelper.enableGlow(data.fluidType).color(data.fluidType, (float) data.fluidType.amount / (float) tileEntity.clientCapacity);
                if (data.fluidType.getFluid().isGaseous(data.fluidType)) {
                    FluidRenderer.getTankDisplay(data).render();
                } else {
                    FluidRenderer.getTankDisplay(data, tileEntity.prevScale).render();
                }

                renderHelper.cleanup();

                for (ValveData valveData : tileEntity.valveViewing) {
                    MekanismRenderHelper valveRenderHelper = FluidRenderer.initHelper();
                    FluidRenderer.translateToOrigin(valveData.location);
                    valveRenderHelper.enableGlow(data.fluidType).color(data.fluidType);
                    FluidRenderer.getValveDisplay(ValveRenderData.get(data, valveData)).render();
                    valveRenderHelper.cleanup();
                }
            }
        }
    }
}