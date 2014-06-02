package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.client.sound.SoundHandler;
import mekanism.common.inventory.container.ContainerDictionary;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDictionary extends GuiMekanism
{
	public ItemStack itemType;

	public List<String> oreDictNames;

	public GuiDictionary(InventoryPlayer inventory)
	{
		super(new ContainerDictionary(inventory));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(MekanismUtils.localize("item.Dictionary.name"), 64, 5, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, ySize - 96 + 2, 0x404040);

		if(itemType != null)
		{
			if(!oreDictNames.isEmpty())
			{
				int currentY = 57;

				for(String name : oreDictNames)
				{
					fontRendererObj.drawString(MekanismUtils.localize("gui.dictionary.key") + ": " + name, 9, currentY, 0x00CD00);
					currentY += 9;
				}
			}
			else {
				fontRendererObj.drawString(MekanismUtils.localize("gui.dictionary.noKey"), 9, 57, 0x00CD00);
			}
		}

		if(itemType != null)
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), itemType, 80, 23);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiDictionary.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;

		if(xAxis >= 80 && xAxis <= 96 && yAxis >= 23 && yAxis <= 39)
		{
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);

			int x = guiWidth + 80;
			int y = guiHeight + 23;
			drawGradientRect(x, y, x + 16, y + 16, -2130706433, -2130706433);

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glPopMatrix();
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(button == 0)
		{
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				Slot hovering = null;

				for(int i = 0; i < inventorySlots.inventorySlots.size(); i++)
				{
					Slot slot = (Slot)inventorySlots.inventorySlots.get(i);

					if(isMouseOverSlot(slot, mouseX, mouseY))
					{
						hovering = slot;
						break;
					}
				}

				if(hovering != null)
				{
					ItemStack stack = hovering.getStack();

					if(stack != null)
					{
						itemType = stack.copy();
						itemType.stackSize = 1;

						oreDictNames = MekanismUtils.getOreDictName(itemType);
						SoundHandler.playSound("gui.button.press");
						
						return;
					}
				}
			}

			if(xAxis >= 80 && xAxis <= 96 && yAxis >= 23 && yAxis <= 39)
			{
				ItemStack stack = mc.thePlayer.inventory.getItemStack();

				if(stack != null && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					itemType = stack.copy();
					itemType.stackSize = 1;

					oreDictNames = MekanismUtils.getOreDictName(itemType);
				}
				else if(stack == null && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					itemType = null;
					oreDictNames = new ArrayList<String>();
				}

                SoundHandler.playSound("gui.button.press");
			}
		}

		super.mouseClicked(mouseX, mouseY, button);
	}
}
