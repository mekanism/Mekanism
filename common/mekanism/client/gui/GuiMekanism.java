package mekanism.client.gui;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public abstract class GuiMekanism extends GuiContainer
{
	public Set<GuiElement> guiElements = new HashSet<GuiElement>();
	
	public GuiMekanism(Container container)
	{
		super(container);
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
}
