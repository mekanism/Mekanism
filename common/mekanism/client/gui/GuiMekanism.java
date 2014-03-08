package mekanism.client.gui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.common.IInvConfiguration;
import mekanism.common.SideData;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.TileEntityContainerBlock;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

public abstract class GuiMekanism extends GuiContainer
{
	public Set<GuiElement> guiElements = new HashSet<GuiElement>();

	private TileEntityContainerBlock tileEntity;

	public GuiMekanism(Container container)
	{
		super(container);
	}

	public GuiMekanism(TileEntityContainerBlock tile, Container container)
	{
		super(container);
		tileEntity = tile;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		for(GuiElement element : guiElements)
		{
			element.renderForeground(xAxis, yAxis);
		}

		if(tileEntity != null)
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

			ItemStack stack = mc.thePlayer.inventory.getItemStack();

			if(stack != null && stack.getItem() instanceof ItemConfigurator && hovering != null)
			{
				SideData data = getFromSlot(hovering);

				if(data != null)
				{
					drawCreativeTabHoveringText(data.color.getName(), xAxis, yAxis);
				}
			}
		}
	}

	private SideData getFromSlot(Slot slot)
	{
		if(slot.slotNumber < tileEntity.getSizeInventory())
		{
			IInvConfiguration config = (IInvConfiguration)tileEntity;

			for(SideData data : config.getSideData())
			{
				for(int id : data.availableSlots)
				{
					if(id == slot.getSlotIndex())
					{
						return data;
					}
				}
			}
		}

		return null;
	}

	protected boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY)
	{
		return isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;

		for(GuiElement element : guiElements)
		{
			element.renderBackground(xAxis, yAxis, guiWidth, guiHeight);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		for(GuiElement element : guiElements)
		{
			element.preMouseClicked(xAxis, yAxis, button);
		}

		super.mouseClicked(mouseX, mouseY, button);

		for(GuiElement element : guiElements)
		{
			element.mouseClicked(xAxis, yAxis, button);
		}
	}

	@Override
	protected void drawCreativeTabHoveringText(String text, int x, int y)
	{
		func_102021_a(Arrays.asList(new String[] {text}), x, y);
	}

	@Override
	protected void func_102021_a(List list, int x, int y)
	{
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT + GL11.GL_LIGHTING_BIT);
		super.func_102021_a(list, x, y);
		GL11.glPopAttrib();
	}

	protected FontRenderer getFontRenderer()
	{
		return fontRenderer;
	}
}
