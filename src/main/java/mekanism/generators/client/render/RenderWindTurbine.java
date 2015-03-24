package mekanism.generators.client.render;

import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelWindTurbine;
import mekanism.generators.common.tile.TileEntityWindTurbine;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class RenderWindTurbine extends TileEntitySpecialRenderer
{
	private ModelWindTurbine model = new ModelWindTurbine();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityWindTurbine)tileEntity, x, y, z, partialTick);
	}

	private void renderAModelAt(TileEntityWindTurbine tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "WindTurbine.png"));

		switch(tileEntity.facing)
		{
			case 2: GL11.glRotatef(0, 0.0F, 1.0F, 0.0F); break;
			case 3: GL11.glRotatef(180, 0.0F, 1.0F, 0.0F); break;
			case 4: GL11.glRotatef(90, 0.0F, 1.0F, 0.0F); break;
			case 5: GL11.glRotatef(270, 0.0F, 1.0F, 0.0F); break;
		}

		GL11.glRotatef(180, 0F, 0F, 1F);

		if(!Mekanism.proxy.isPaused() &&
				tileEntity.getWorldObj().canBlockSeeTheSky(tileEntity.xCoord, tileEntity.yCoord+4, tileEntity.zCoord))
		{
			tileEntity.angle = (tileEntity.angle+((tileEntity.yCoord+4)/256F)*8) % 360;
		}

		model.render(0.0625F, tileEntity.angle);
		GL11.glPopMatrix();
	}
}
