package mekanism.client;

import org.lwjgl.opengl.GL11;

import mekanism.common.TileEntityElectricPump;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderElectricPump extends TileEntitySpecialRenderer
{
	private ModelElectricPump model = new ModelElectricPump();
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityElectricPump)tileEntity, x, y, z, 1F);
	}
	
	private void renderAModelAt(TileEntityElectricPump tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		bindTextureByName("/mods/mekanism/render/ElectricPump.png");
		
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F);
		GL11.glPopMatrix();
	}
}
