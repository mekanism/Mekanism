package mekanism.client.render.tileentity;

import mekanism.client.model.ModelLogisticalSorter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderLogisticalSorter extends TileEntitySpecialRenderer
{
	private ModelLogisticalSorter model = new ModelLogisticalSorter();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityLogisticalSorter)tileEntity, x, y, z, partialTick);
	}

	private void renderAModelAt(TileEntityLogisticalSorter tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);

		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LogisticalSorter" + (tileEntity.isActive ? "On" : "") + ".png"));

		switch(tileEntity.facing)
		{
			case 0:
			{
				GL11.glRotatef(90F, 0.0F, 0.0F, -1.0F);
				GL11.glTranslatef(1.0F, 1.0F, 0.0F);
				break;
			}
			case 1:
			{
				GL11.glRotatef(90F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslatef(-1.0F, 1.0F, 0.0F);
				break;
			}
			case 2: GL11.glRotatef(90, 0.0F, 1.0F, 0.0F); break;
			case 3: GL11.glRotatef(270, 0.0F, 1.0F, 0.0F); break;
			case 4: GL11.glRotatef(180, 0.0F, 1.0F, 0.0F); break;
			case 5: GL11.glRotatef(0, 0.0F, 1.0F, 0.0F); break;
		}

		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F, tileEntity.isActive);
		GL11.glPopMatrix();
	}
}
