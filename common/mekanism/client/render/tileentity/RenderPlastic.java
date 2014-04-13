package mekanism.client.render.tileentity;

import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityPlasticBlock;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;


@SideOnly(Side.CLIENT)
public class RenderPlastic
{
	public static Model3D toRender;

	static
	{
		toRender = new Model3D();
		toRender.setBlockBounds(0,0,0,1,1,1);
	}

	public static void renderItem(ItemStack stack)
	{
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);

		int meta = stack.getItemDamage();
		toRender.setTexture(Mekanism.BlockHDPE.getIcon(0, meta));
		EnumColor color = EnumColor.DYES[meta&15];
		float[] colour = {color.getColor(0), color.getColor(1), color.getColor(2)};
		GL11.glColor3f(colour[0], colour[1], colour[2]);
		GL11.glTranslatef(0F, -0.1F, 0F);
		RenderHelper.disableStandardItemLighting();
		MekanismRenderer.renderObject(toRender);

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}