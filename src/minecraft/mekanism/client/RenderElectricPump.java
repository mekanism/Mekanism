package mekanism.client;

import org.lwjgl.opengl.GL11;

import mekanism.common.TileEntityElectricPump;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderElectricPump extends TileEntitySpecialRenderer
{
	private ModelElectricPump model = new ModelElectricPump();
	
	@Override
	public void renderTileEntityAt(TileEntity var1, double var2, double var4, double var6, float var8)
	{
		renderAModelAt((TileEntityElectricPump) var1, var2, var4, var6, 1F);
	}
	
	private void renderAModelAt(TileEntityElectricPump tileEntity, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5f, (float) y + 1.5f, (float) z + 0.5f);
		bindTextureByName("/mods/mekanism/render/ElectricPump.png");
		
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F);
		GL11.glPopMatrix();
	}
}
