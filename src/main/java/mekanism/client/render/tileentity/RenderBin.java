package mekanism.client.render.tileentity;

import mekanism.api.Coord4D;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityBin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBin extends TileEntitySpecialRenderer<TileEntityBin>
{
	private final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

	@Override
	public void renderTileEntityAt(TileEntityBin tileEntity, double x, double y, double z, float partialTick, int destroyStage)
	{
		String amount = "";
		ItemStack itemStack = tileEntity.itemType;

		if(itemStack != null)
		{
			amount = Integer.toString(tileEntity.clientAmount);
		}

		Coord4D obj = Coord4D.get(tileEntity).offset(tileEntity.facing);

		if(obj.getBlockState(tileEntity.getWorld()).getBlock().isSideSolid(tileEntity.getWorld(), obj.getPos(), tileEntity.facing.getOpposite()))
		{
			return;
		}

		MekanismRenderer.glowOn();

		if(itemStack != null)
		{
			GL11.glPushMatrix();

			switch(tileEntity.facing)
			{
				case NORTH:
					GL11.glTranslated(x + 0.73, y + 0.83, z - 0.01);
					break;
				case SOUTH:
					GL11.glTranslated(x + 0.27, y + 0.83, z + 1.01);
					GL11.glRotatef(180, 0, 1, 0);
					break;
				case WEST:
					GL11.glTranslated(x - 0.01, y + 0.83, z + 0.27);
					GL11.glRotatef(90, 0, 1, 0);
					break;
				case EAST:
					GL11.glTranslated(x + 1.01, y + 0.83, z + 0.73);
					GL11.glRotatef(-90, 0, 1, 0);
					break;
			}

			float scale = 0.03125F;
			float scaler = 0.9F;

			GL11.glScalef(scale*scaler, scale*scaler, -0.0001F);
			GL11.glRotatef(180, 0, 0, 1);

			renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0);

			GL11.glPopMatrix();
		}

		if(!amount.equals(""))
		{
			renderText(amount, tileEntity.facing, 0.02F, x, y - 0.31F, z);
		}

		MekanismRenderer.glowOff();
	}

	@SuppressWarnings("incomplete-switch")
	private void renderText(String text, EnumFacing side, float maxScale, double x, double y, double z)
	{
		GL11.glPushMatrix();

		GL11.glPolygonOffset(-10, -10);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);

		float displayWidth = 1 - (2 / 16);
		float displayHeight = 1 - (2 / 16);
		GL11.glTranslated(x, y, z);

		switch(side)
		{
			case SOUTH:
				GL11.glTranslatef(0, 1, 0);
				GL11.glRotatef(0, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);
				break;
			case NORTH:
				GL11.glTranslatef(1, 1, 1);
				GL11.glRotatef(180, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);
				break;
			case EAST:
				GL11.glTranslatef(0, 1, 1);
				GL11.glRotatef(90, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);
				break;
			case WEST:
				GL11.glTranslatef(1, 1, 0);
				GL11.glRotatef(-90, 0, 1, 0);
				GL11.glRotatef(90, 1, 0, 0);
				break;
		}

		GL11.glTranslatef(displayWidth / 2, 1F, displayHeight / 2);
		GL11.glRotatef(-90, 1, 0, 0);

		FontRenderer fontRenderer = getFontRenderer();

		int requiredWidth = Math.max(fontRenderer.getStringWidth(text), 1);
		int lineHeight = fontRenderer.FONT_HEIGHT + 2;
		int requiredHeight = lineHeight * 1;
		float scaler = 0.4F;
		float scaleX = (displayWidth / requiredWidth);
		float scale = scaleX * scaler;

		if(maxScale > 0)
		{
			scale = Math.min(scale, maxScale);
		}

		GL11.glScalef(scale, -scale, scale);
		GL11.glDepthMask(false);

		int realHeight = (int)Math.floor(displayHeight / scale);
		int realWidth = (int)Math.floor(displayWidth / scale);

		int offsetX = (realWidth - requiredWidth) / 2;
		int offsetY = (realHeight - requiredHeight) / 2;

		GL11.glDisable(GL11.GL_LIGHTING);
		fontRenderer.drawString("\u00a7f" + text, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

		GL11.glPopMatrix();
	}
}
