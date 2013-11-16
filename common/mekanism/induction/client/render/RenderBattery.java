/**
 * 
 */
package mekanism.induction.client.render;

import java.util.Random;

import mekanism.induction.client.model.ModelBattery;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.tileentity.TileEntityBattery;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderBattery extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(MekanismInduction.DOMAIN, MekanismInduction.MODEL_TEXTURE_DIRECTORY + "battery.png");
	public static final ResourceLocation TEXTURE_MULTI = new ResourceLocation(MekanismInduction.DOMAIN, MekanismInduction.MODEL_TEXTURE_DIRECTORY + "battery_multi.png");

	public static final ModelBattery MODEL = new ModelBattery();
	private EntityItem fakeBattery;
	private Random random = new Random();
	protected RenderManager renderManager;

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

		if (((TileEntityBattery) t).structure.isMultiblock)
		{
			this.bindTexture(TEXTURE_MULTI);
		}
		else
		{
			this.bindTexture(TEXTURE);
		}

		MODEL.render(0.0625f);
		/*
		 * GL11.glScalef(0.0625f, 0.0625f, 0.0625f); batteryModel.renderAll();
		 */
		GL11.glPopMatrix();

		if (Minecraft.getMinecraft().gameSettings.fancyGraphics)
		{
			if (this.fakeBattery == null)
			{
				this.fakeBattery = new EntityItem(t.worldObj, 0, 0, 0, new ItemStack(MekanismInduction.Capacitor));
				this.fakeBattery.age = 10;
			}

			if (this.renderManager == null)
			{
				this.renderManager = RenderManager.instance;
			}

			int renderAmount = Math.min(((TileEntityBattery) t).clientCells, 16);

			if (renderAmount == 0)
			{
				return;
			}

			for (int i = 2; i < 6; i++)
			{
				ForgeDirection direction = ForgeDirection.getOrientation(i);

				for (int slot = 0; slot < 4; slot++)
				{
					Vector3 sideVec = new Vector3(t).modifyPositionFromSide(correctSide(direction));
					Block block = Block.blocksList[sideVec.getBlockID(t.worldObj)];

					if (block != null && block.isOpaqueCube())
					{
						continue;
					}

					GL11.glPushMatrix();
					GL11.glTranslatef((float) x + 0.5f, (float) y + 0.7f, (float) z + 0.5f);

					float translateX = 0;
					float translateY = 0;

					switch (slot)
					{
						case 0:
							translateX = 0.25f;
							break;
						case 1:
							translateX = 0.25f;
							translateY = -0.5f;
							break;
						case 2:
							translateX = -0.25f;
							translateY = -0.5f;
							break;
						case 3:
							translateX = -0.25f;
							break;
					}

					switch (direction)
					{
						case NORTH:
							GL11.glTranslatef(-0.5f, 0, 0);
							GL11.glTranslatef(0, translateY, translateX);
							GL11.glRotatef(90, 0, 1, 0);
							break;
						case SOUTH:
							GL11.glTranslatef(0, 0, -0.5f);
							GL11.glTranslatef(translateX, translateY, 0);
							break;
						case WEST:
							GL11.glTranslatef(0.5f, 0, 0);
							GL11.glTranslatef(0, translateY, translateX);
							GL11.glRotatef(90, 0, 1, 0);
							break;
						case EAST:
							GL11.glTranslatef(0, 0, 0.5f);
							GL11.glTranslatef(translateX, translateY, 0);
							break;
					}

					GL11.glScalef(0.5f, 0.5f, 0.5f);

					this.renderItemSimple(this.fakeBattery);
					GL11.glPopMatrix();

					if (--renderAmount <= 0)
					{
						return;
					}
				}
			}
		}
	}

	private ForgeDirection correctSide(ForgeDirection side)
	{
		switch (side)
		{
			case NORTH:
				return ForgeDirection.WEST;
			case SOUTH:
				return ForgeDirection.NORTH;
			case EAST:
				return ForgeDirection.SOUTH;
			case WEST:
				return ForgeDirection.EAST;
			default:
				return null;
		}
	}

	public void renderItemSimple(EntityItem entityItem)
	{
		if (entityItem != null)
		{
			Tessellator tessellator = Tessellator.instance;
			ItemStack itemStack = entityItem.getEntityItem();

			for (int k = 0; k < itemStack.getItem().getRenderPasses(itemStack.getItemDamage()); ++k)
			{
				Icon icon = itemStack.getItem().getIcon(itemStack, k);

				if (icon == null)
				{
					TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
					ResourceLocation resourcelocation = texturemanager.getResourceLocation(entityItem.getEntityItem().getItemSpriteNumber());
					icon = ((TextureMap) texturemanager.getTexture(resourcelocation)).getAtlasSprite("missingno");
				}

				float f4 = icon.getMinU();
				float f5 = icon.getMaxU();
				float f6 = icon.getMinV();
				float f7 = icon.getMaxV();
				float f8 = 1.0F;
				float f9 = 0.5F;
				float f10 = 0.25F;
				float f11;

				GL11.glPushMatrix();

				float f12 = 0.0625F;
				f11 = 0.021875F;
				ItemStack itemstack = entityItem.getEntityItem();
				int j = itemstack.stackSize;
				byte b0 = getMiniItemCount(itemstack);

				GL11.glTranslatef(-f9, -f10, -((f12 + f11) * b0 / 2.0F));

				for (int kj = 0; kj < b0; ++kj)
				{
					// Makes items offset when in 3D, like when in 2D, looks much better. Considered
					// a
					// vanilla bug...
					if (kj > 0)
					{
						float x = (random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
						float y = (random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
						float z = (random.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
						GL11.glTranslatef(x, y, f12 + f11);
					}
					else
					{
						GL11.glTranslatef(0f, 0f, f12 + f11);
					}

					if (itemstack.getItemSpriteNumber() == 0)
					{
						this.bindTexture(TextureMap.locationBlocksTexture);
					}
					else
					{
						this.bindTexture(TextureMap.locationItemsTexture);
					}

					GL11.glColor4f(1, 1, 1, 1.0F);
					ItemRenderer.renderItemIn2D(tessellator, f5, f6, f4, f7, icon.getIconWidth(), icon.getIconHeight(), f12);
				}

				GL11.glPopMatrix();
			}
		}

	}

	public byte getMiniItemCount(ItemStack stack)
	{
		byte ret = 1;
		if (stack.stackSize > 1)
			ret = 2;
		if (stack.stackSize > 15)
			ret = 3;
		if (stack.stackSize > 31)
			ret = 4;
		return ret;
	}
}
