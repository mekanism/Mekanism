package mekanism.client.render.tileentity;

import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderElectricChest extends TileEntitySpecialRenderer
{
	private ModelChest model = new ModelChest();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityElectricChest)tileEntity, x, y, z, partialTick);
	}

	private void renderAModelAt(TileEntityElectricChest tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x, (float)y + 1.0F, (float)z);
		GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
		bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ElectricChest.png"));

		switch(tileEntity.facing)
		{
			case 2:
				GL11.glRotatef(270, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(1.0F, 0.0F, 0.0F);
				break;
			case 3:
				GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, 0.0F, -1.0F);
				break;
			case 4:
				GL11.glRotatef(0, 0.0F, 1.0F, 0.0F);
				break;
			case 5:
				GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(1.0F, 0.0F, -1.0F);
				break;
		}

		float lidangle = tileEntity.prevLidAngle + (tileEntity.lidAngle - tileEntity.prevLidAngle) * partialTick;
		lidangle = 1.0F - lidangle;
		lidangle = 1.0F - lidangle * lidangle * lidangle;
		model.chestLid.rotateAngleX = -((lidangle * 3.141593F) / 2.0F);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		model.renderAll();
		GL11.glPopMatrix();
	}
}
