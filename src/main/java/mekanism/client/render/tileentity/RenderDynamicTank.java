package mekanism.client.render.tileentity;

import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.FluidRenderer.ValveRenderData;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
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

            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            if (data.location != null && data.height >= 1) {
                MekanismRenderHelper renderHelper = FluidRenderer.initHelper();
                FluidRenderer.translateToOrigin(data.location);
                renderHelper.enableGlow(tileEntity.structure.fluidStored).color(tileEntity.structure.fluidStored);
                if (tileEntity.structure.fluidStored.getFluid().isGaseous()) {
                    //TODO: Does the color alpha overwrite the color set based on fluid
                    renderHelper.colorAlpha(Math.min(1, ((float) tileEntity.structure.fluidStored.amount / (float) tileEntity.clientCapacity) + MekanismRenderer.GAS_RENDER_BASE));
                    FluidRenderer.getTankDisplay(data).render();
                } else {
                    FluidRenderer.getTankDisplay(data, tileEntity.prevScale).render();
                }

                renderHelper.cleanup();

                for (ValveData valveData : tileEntity.valveViewing) {
                    MekanismRenderHelper valveRenderHelper = FluidRenderer.initHelper();
                    FluidRenderer.translateToOrigin(valveData.location);
                    valveRenderHelper.enableGlow(tileEntity.structure.fluidStored).color(tileEntity.structure.fluidStored);
                    FluidRenderer.getValveDisplay(ValveRenderData.get(data, valveData)).render();
                    valveRenderHelper.cleanup();
                }
            }
        }
    }
}