/**
 * 
 */
package mekanism.induction.client.render;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.induction.client.model.ModelTeslaBottom;
import mekanism.induction.client.model.ModelTeslaMiddle;
import mekanism.induction.client.model.ModelTeslaTop;
import mekanism.induction.common.MekanismInduction;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderTesla extends TileEntitySpecialRenderer
{
	public static final ModelTeslaBottom bottom = new ModelTeslaBottom();
	public static final ModelTeslaMiddle middle = new ModelTeslaMiddle();
	public static final ModelTeslaTop top = new ModelTeslaTop();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

		switch (t.getBlockMetadata())
		{
			default:
				bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "TeslaBottom.png"));
				bottom.render(0.0625f);
				break;
			case 1:
				bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "TeslaMiddle.png"));
				middle.render(0.0625f);
				break;
			case 2:
				bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "TeslaTop.png"));
				top.render(0.0625f);
				break;
		}

		GL11.glPopMatrix();
	}

}
