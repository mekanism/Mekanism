package mekanism.client.render.tileentity;

import mekanism.client.model.ModelSolarNeutronActivator;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSolarNeutronActivator extends TileEntitySpecialRenderer<TileEntitySolarNeutronActivator> {

    private ModelSolarNeutronActivator model = new ModelSolarNeutronActivator();

    @Override
    public void render(TileEntitySolarNeutronActivator tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SolarNeutronActivator.png"));

        MekanismRenderer.glRotateForFacing(tileEntity);

        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);

        model.render(0.0625F);
        renderHelper.cleanup();
    }
}