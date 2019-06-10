package mekanism.client.render.tileentity;

import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderThermalEvaporationController extends TileEntitySpecialRenderer<TileEntityThermalEvaporationController> {

    @Override
    public void render(TileEntityThermalEvaporationController tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (tileEntity.structured && tileEntity.inputTank.getFluid() != null && tileEntity.height - 2 >= 1 && tileEntity.inputTank.getFluidAmount() > 0) {
            RenderData data = new RenderData();
            data.location = tileEntity.getRenderLocation();
            data.height = tileEntity.height - 2;
            //TODO: If we ever allow different width for the evap controller then update this length and width
            data.length = 2;
            data.width = 2;
            data.fluidType = tileEntity.inputTank.getFluid();
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            MekanismRenderHelper renderHelper = FluidRenderer.initHelper();
            FluidRenderer.translateToOrigin(data.location);
            renderHelper.enableGlow(tileEntity.inputTank.getFluid()).color(tileEntity.inputTank.getFluid());
            if (tileEntity.inputTank.getFluid().getFluid().isGaseous()) {
                renderHelper.colorAlpha(Math.min(1, ((float) tileEntity.inputTank.getFluidAmount() / (float) tileEntity.getMaxFluid()) + MekanismRenderer.GAS_RENDER_BASE));
                FluidRenderer.getTankDisplay(data).render();
            } else {
                //Render the proper height
                FluidRenderer.getTankDisplay(data, Math.min((float) tileEntity.inputTank.getFluidAmount() / tileEntity.getMaxFluid(), 1)).render();
            }
            renderHelper.cleanup();
        }
    }
}