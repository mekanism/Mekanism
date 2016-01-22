package mekanism.generators.client.render;

import org.lwjgl.opengl.GL11;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRod;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTurbineRod extends TileEntitySpecialRenderer
{
	private ModelTurbine model = new ModelTurbine();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityTurbineRod)tileEntity, x, y, z, partialTick);
	}

	private void renderAModelAt(TileEntityTurbineRod tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Turbine.png"));
		
		if(tileEntity.getHousedBlades() > 0)
		{
			GL11.glTranslated(x + 0.5, y - 1, z + 0.5);
			model.render(0.0625F, tileEntity.clientIndex*2);
		}
		
		if(tileEntity.getHousedBlades() == 2)
		{
			GL11.glTranslatef(0.0F, 0.5F, 0.0F);
			model.render(0.0625F, (tileEntity.clientIndex*2)+1);
		}
		
		GL11.glPopMatrix();
	}
}
