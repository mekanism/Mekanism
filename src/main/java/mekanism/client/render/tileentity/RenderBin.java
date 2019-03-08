package mekanism.client.render.tileentity;

import mekanism.api.Coord4D;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.LangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
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
	public void render(TileEntityBin tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha)
	{
		Coord4D obj = Coord4D.get(tileEntity).offset(tileEntity.facing);

		if(obj.getBlockState(tileEntity.getWorld()).isSideSolid(tileEntity.getWorld(), obj.getPos(), tileEntity.facing.getOpposite()))
		{
			return;
		}
		
		render(tileEntity.facing, tileEntity.itemType, tileEntity.clientAmount, true, x, y, z);
	}
	
	public void render(EnumFacing facing, ItemStack itemType, int clientAmount, boolean text, double x, double y, double z)
	{
		String amount = "";
		
		if(!itemType.isEmpty())
		{
			amount = Integer.toString(clientAmount);
			
			if(clientAmount == Integer.MAX_VALUE)
			{
				amount = LangUtils.localize("gui.infinite");
			}
			
			GlStateManager.pushMatrix();

			switch(facing)
			{
				case NORTH:
					GL11.glTranslated(x + 0.73, y + 0.83, z - 0.0001);
					break;
				case SOUTH:
					GL11.glTranslated(x + 0.27, y + 0.83, z + 1.0001);
					GlStateManager.rotate(180, 0, 1, 0);
					break;
				case WEST:
					GL11.glTranslated(x - 0.0001, y + 0.83, z + 0.27);
					GlStateManager.rotate(90, 0, 1, 0);
					break;
				case EAST:
					GL11.glTranslated(x + 1.0001, y + 0.83, z + 0.73);
					GlStateManager.rotate(-90, 0, 1, 0);
					break;
				default:
					break;
			}

			float scale = 0.03125F;
			float scaler = 0.9F;

			GlStateManager.scale(scale*scaler, scale*scaler, -0.0001F);
			GlStateManager.rotate(180, 0, 0, 1);

			renderItem.renderItemAndEffectIntoGUI(itemType, 0, 0);
			
			GlStateManager.popMatrix();
		}

		if(text && !amount.equals(""))
		{
			renderText(amount, facing, 0.02F, x, y - 0.3725F, z);
		}
	}

	@SuppressWarnings("incomplete-switch")
	private void renderText(String text, EnumFacing side, float maxScale, double x, double y, double z)
	{
		GlStateManager.pushMatrix();

		GlStateManager.doPolygonOffset(-10, -10);
		GlStateManager.enablePolygonOffset();

		float displayWidth = 1 - (2 / 16);
		float displayHeight = 1 - (2 / 16);
		GlStateManager.translate(x, y, z);

		switch(side)
		{
			case SOUTH:
				GlStateManager.translate(0, 1, 0);
				GlStateManager.rotate(0, 0, 1, 0);
				GlStateManager.rotate(90, 1, 0, 0);
				break;
			case NORTH:
				GlStateManager.translate(1, 1, 1);
				GlStateManager.rotate(180, 0, 1, 0);
				GlStateManager.rotate(90, 1, 0, 0);
				break;
			case EAST:
				GlStateManager.translate(0, 1, 1);
				GlStateManager.rotate(90, 0, 1, 0);
				GlStateManager.rotate(90, 1, 0, 0);
				break;
			case WEST:
				GlStateManager.translate(1, 1, 0);
				GlStateManager.rotate(-90, 0, 1, 0);
				GlStateManager.rotate(90, 1, 0, 0);
				break;
		}

		GlStateManager.translate(displayWidth / 2, 1F, displayHeight / 2);
		GlStateManager.rotate(-90, 1, 0, 0);

		FontRenderer fontRenderer = getFontRenderer();

		int requiredWidth = Math.max(fontRenderer.getStringWidth(text), 1);
		int lineHeight = fontRenderer.FONT_HEIGHT + 2;
		int requiredHeight = lineHeight;
		float scaler = 0.4F;
		float scaleX = (displayWidth / requiredWidth);
		float scale = scaleX * scaler;

		if(maxScale > 0)
		{
			scale = Math.min(scale, maxScale);
		}

		GlStateManager.scale(scale, -scale, scale);
		GlStateManager.depthMask(false);

		int realHeight = (int)Math.floor(displayHeight / scale);
		int realWidth = (int)Math.floor(displayWidth / scale);

		int offsetX = (realWidth - requiredWidth) / 2;
		int offsetY = (realHeight - requiredHeight) / 2;

		GlStateManager.disableLighting();
		fontRenderer.drawString("\u00a7f" + text, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.disablePolygonOffset();

		GlStateManager.popMatrix();
	}
}
