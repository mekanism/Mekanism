package mekanism.client.nei;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.gui.inventory.GuiContainer;
import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.api.INEIGuiAdapter;

public class ElementBoundHandler extends INEIGuiAdapter
{
	@Override
	public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int width, int height)
	{
		if(gui instanceof GuiMekanism)
		{
			GuiMekanism guiMek = (GuiMekanism)gui;
			
			Rectangle4i rect = new Rectangle4i(x, y, width, height);
			
			for(GuiElement element : guiMek.guiElements)
			{
				if(element.getBounds(guiMek.getXPos(), guiMek.getYPos()).intersects(rect)) 
				{
					return true;
				}
			}
		}
		
		return false;
	}
}
