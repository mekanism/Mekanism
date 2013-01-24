package mekanism.api;

import mekanism.common.EnumColor;

public class SideData 
{
	public EnumColor color;
	
	public int slotStart;
	
	public int slotAmount;
	
	public SideData(EnumColor colour, int start, int amount)
	{
		color = colour;
		slotStart = start;
		slotAmount = amount;
	}
}
