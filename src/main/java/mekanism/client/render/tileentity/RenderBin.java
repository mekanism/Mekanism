package mekanism.client.render.tileentity;

import mekanism.api.Coord4D;
import mekanism.common.tile.TileEntityBin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderBin extends TileEntitySpecialRenderer
{
	private final RenderBlocks renderBlocks = new RenderBlocks();
	private final RenderItem renderItem = (RenderItem)RenderManager.instance.getEntityClassRenderObject(EntityItem.class);

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
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

			Coord4D obj = Coord4D.get(tileEntity).getFromSide(ForgeDirection.getOrientation(tileEntity.facing));

			if(tileEntity.getWorldObj().getBlock(obj.xCoord, obj.yCoord, obj.zCoord).isSideSolid(tileEntity.getWorldObj(), obj.xCoord, obj.yCoord, obj.zCoord, ForgeDirection.getOrientation(tileEntity.facing).getOpposite()))
			{
				return;
			}

			doLight(tileEntity.getWorldObj(), obj);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

			if(itemStack != null)
			{
				GL11.glPushMatrix();

				switch(ForgeDirection.getOrientation(tileEntity.facing))
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

				TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);

				renderItem.renderItemAndEffectIntoGUI(func_147498_b()/*getFontRenderer()*/, renderEngine, itemStack, 0, 0);
				
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glPopMatrix();
			}

			if(amount != "")
			{
				renderText(amount, ForgeDirection.getOrientation(tileEntity.facing), 0.02F, x, y - 0.31F, z);
			}
		}
	}

	private void doLight(World world, Coord4D obj)
	{
		if(world.getBlock(obj.xCoord, obj.yCoord, obj.zCoord).isOpaqueCube())
		{
			return;
		}

		int brightness = world.getLightBrightnessForSkyBlocks(obj.xCoord, obj.yCoord, obj.zCoord, 0);
		int lightX = brightness % 65536;
		int lightY = brightness / 65536;
		float scale = 0.6F;

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX * scale, lightY * scale);
	}

	@SuppressWarnings("incomplete-switch")
	private void renderText(String text, ForgeDirection side, float maxScale, double x, double y, double z)
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

		FontRenderer fontRenderer = func_147498_b();//getFontRenderer();

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
