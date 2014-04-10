package mekanism.client.render.tileentity;

import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityPlasticBlock;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;


@SideOnly(Side.CLIENT)
public class RenderPlastic extends TileEntitySpecialRenderer
{
	public static Model3D toRender;

	static
	{
		toRender = new Model3D();
		toRender.setBlockBounds(0,0,0,1,1,1);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityPlasticBlock) tileEntity, x, y, z, partialTick);
	}

	public void renderAModelAt(TileEntityPlasticBlock plastic, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		bindTexture(MekanismRenderer.getBlocksTexture());

		toRender.setTexture(Mekanism.BlockHDPE.getBlockTexture(plastic.worldObj, plastic.xCoord, plastic.yCoord, plastic.zCoord, 0));
		EnumColor color = EnumColor.DYES[plastic.colour];
		float[] colour = {color.getColor(0), color.getColor(1), color.getColor(2)};
		GL11.glColor3f(colour[0], colour[1], colour[2]);
		RenderHelper.disableStandardItemLighting();
		MekanismRenderer.renderObject(toRender);
		GL11.glPopMatrix();
	}
}