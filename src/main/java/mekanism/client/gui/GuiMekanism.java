package mekanism.client.gui;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public abstract class GuiMekanism extends GuiContainer implements IGuiWrapper
{
	public Set<GuiElement> guiElements = new HashSet<>();

	private TileEntityContainerBlock tileEntity;

	//Try not to use
	public GuiMekanism(Container container)
	{
		super(container);
	}

	public GuiMekanism(TileEntityContainerBlock tile, Container container)
	{
		super(container);
		tileEntity = tile;
	}
	
	public float getNeededScale(String text, int maxX)
	{
		int length = fontRenderer.getStringWidth(text);
		
		if(length <= maxX)
		{
			return 1;
		}
		else {
			return (float)maxX/length;
		}
	}
	
	/** returns scale */
	public void renderScaledText(String text, int x, int y, int color, int maxX)
	{
		int length = fontRenderer.getStringWidth(text);
		
		if(length <= maxX)
		{
			fontRenderer.drawString(text, x, y, color);
		}
		else {
			float scale = (float)maxX/length;
			float reverse = 1/scale;
			float yAdd = 4-(scale*8)/2F;
			
			GlStateManager.pushMatrix();
			
			GlStateManager.scale(scale, scale, scale);
			fontRenderer.drawString(text, (int)(x*reverse), (int)((y*reverse)+yAdd), color);
			
			GlStateManager.popMatrix();
		}
	}
	
	public static boolean isTextboxKey(char c, int i)
	{
		if(i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT ||
				i == Keyboard.KEY_END || i == Keyboard.KEY_HOME || i == Keyboard.KEY_BACK || isKeyComboCtrlA(i) || 
				isKeyComboCtrlC(i) || isKeyComboCtrlV(i) || isKeyComboCtrlX(i))
		{
			return true;
		}
		
		return false;
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

		if(tileEntity instanceof ISideConfiguration)
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

			ItemStack stack = mc.player.inventory.getItemStack();

			if(!stack.isEmpty() && stack.getItem() instanceof ItemConfigurator && hovering != null)
			{
				SideData data = getFromSlot(hovering);

				if(data != null)
				{
					drawHoveringText(data.color + data.localize() + " (" + data.color.getColoredName() + ")", xAxis, yAxis);
				}
			}
		}
	}
	
	public TileEntityContainerBlock getTileEntity()
	{
		return tileEntity;
	}

	private SideData getFromSlot(Slot slot)
	{
		if(slot.slotNumber < tileEntity.getSizeInventory())
		{
			ISideConfiguration config = (ISideConfiguration)tileEntity;

			for(SideData data : config.getConfig().getOutputs(TransmissionType.ITEM))
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
		return isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY);
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
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException
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
	public void drawTexturedRect(int x, int y, int u, int v, int w, int h)
	{
		drawTexturedModalRect(x, y, u, v, w, h);
	}

	@Override
	public void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int w, int h)
	{
		drawTexturedModalRect(x, y, icon, w, h);
	}

	@Override
	public void displayTooltip(String s, int x, int y)
	{
		drawHoveringText(s, x, y);
	}

	@Override
	public void displayTooltips(List<String> list, int xAxis, int yAxis)
	{
		drawHoveringText(list, xAxis, yAxis);
	}

	@Override
	public FontRenderer getFont()
	{
		return fontRenderer;
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks)
	{
		super.mouseClickMove(mouseX, mouseY, button, ticks);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		for(GuiElement element : guiElements)
		{
			element.mouseClickMove(xAxis, yAxis, button, ticks);
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int type)
	{
		super.mouseReleased(mouseX, mouseY, type);
		
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		for(GuiElement element : guiElements)
		{
			element.mouseReleased(xAxis, yAxis, type);
		}
	}
	
	public void handleMouse(Slot slot, int slotIndex, int button, ClickType modifier)
	{
		handleMouseClick(slot, slotIndex, button, modifier);
	}
	
	@Override
	public void handleMouseInput() throws java.io.IOException
	{
		super.handleMouseInput();
		
		int xAxis = Mouse.getEventX() * width / mc.displayWidth - getXPos();
		int yAxis = height - Mouse.getEventY() * height / mc.displayHeight - 1 - getYPos();
		int delta = Mouse.getEventDWheel();
		
		if(delta != 0) 
		{
			mouseWheel(xAxis, yAxis, delta);
		}
	}
	
	public void mouseWheel(int xAxis, int yAxis, int delta)
	{
		for(GuiElement element : guiElements)
		{
			element.mouseWheel(xAxis, yAxis, delta);
		}
	}
	
	public int getXPos()
	{
		return (width - xSize) / 2;
	}
	
	public int getYPos()
	{
		return (height - ySize) / 2;
	}

	protected FontRenderer getFontRenderer()
	{
		return fontRenderer;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}
}
