package mekanism.client.render.tileentity;

import mekanism.api.Coord4D;
import mekanism.common.tile.TileEntityBin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBin extends TileEntitySpecialRenderer
{
	private static final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick, int damage)
	{
		renderAModelAt((TileEntityBin)tileEntity, x, y, z, partialTick);
	}

	@SuppressWarnings("incomplete-switch")
	private void renderAModelAt(TileEntityBin tileEntity, double x, double y, double z, float partialTick)
	{
		if(tileEntity instanceof TileEntityBin)
		{
			String amount = "";
			ItemStack itemStack = tileEntity.itemType;

			if(itemStack != null)
			{
				amount = Integer.toString(tileEntity.clientAmount);
			}

			Coord4D obj = Coord4D.get(tileEntity).offset(EnumFacing.getFront(tileEntity.facing));

			if(tileEntity.getWorld().getBlockState(obj).getBlock().isSideSolid(tileEntity.getWorld(), obj, EnumFacing.getFront(tileEntity.facing).getOpposite()))
			{
				return;
			}

			doLight(tileEntity.getWorld(), obj);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

			if(itemStack != null)
			{
				GL11.glPushMatrix();

				switch(EnumFacing.getFront(tileEntity.facing))
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

				GL11.glScalef(scale*scaler, scale*scaler, 0);
				GL11.glRotatef(180, 0, 0, 1);

				GL11.glDisable(GL11.GL_LIGHTING);

				renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0);

				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glPopMatrix();
			}

			if(amount != "")
			{
				renderText(amount, EnumFacing.getFront(tileEntity.facing), 0.02F, x, y - 0.31F, z);
			}
		}
	}

	private void doLight(World world, Coord4D obj)
	{
		if(world.getBlockState(obj).getBlock().isOpaqueCube())
		{
			return;
		}

		int brightness = world.getCombinedLight(obj, 0);
		int lightX = brightness % 65536;
		int lightY = brightness / 65536;
		float scale = 0.6F;

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX * scale, lightY * scale);
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

		int offsetX;
		int offsetY;
		int realHeight = (int)Math.floor(displayHeight / scale);
		int realWidth = (int)Math.floor(displayWidth / scale);

		offsetX = (realWidth - requiredWidth) / 2;
		offsetY = (realHeight - requiredHeight) / 2;

		GL11.glDisable(GL11.GL_LIGHTING);
		fontRenderer.drawString("\u00a7f" + text, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

		GL11.glPopMatrix();
	}
}
