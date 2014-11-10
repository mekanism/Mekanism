package mekanism.client.nei;

import mekanism.client.gui.GuiMekanism;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import codechicken.nei.guihook.IContainerSlotClickHandler;

public class MekanismSlotClickHandler implements IContainerSlotClickHandler
{
	@Override
	public void beforeSlotClick(GuiContainer gui, int slotIndex, int button, Slot slot, int modifier) 
	{
		
	}

	@Override
	public boolean handleSlotClick(GuiContainer gui, int slotIndex, int button, Slot slot, int modifier, boolean eventconsumed)
	{
		if(gui instanceof GuiMekanism)
		{
			((GuiMekanism)gui).handleMouse(slot, slotIndex, button, modifier);
			return true;
		}
		
		return false;
	}

	@Override
	public void afterSlotClick(GuiContainer gui, int slotIndex, int button, Slot slot, int modifier)
	{
		
	}
}
