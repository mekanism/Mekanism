package mekanism.common;

import mekanism.api.EnumColor;
import mekanism.common.util.MekanismUtils;

public class SideData
{
	/** The color of this SideData */
	public EnumColor color;
	
	/** The name of this SideData */
	public String name;

	/** Int[] of available side slots, can be used for items, gases, or items */
	public int[] availableSlots;
	
	/** EnergyState representing this SideData */
	public EnergyState energyState;

	public SideData(String n, EnumColor colour, int[] slots)
	{
		name = n;
		color = colour;
		availableSlots = slots;
	}
	
	public SideData(String n, EnumColor colour, EnergyState state)
	{
		name = n;
		color = colour;
		energyState = state;
	}
	
	public String localize()
	{
		return MekanismUtils.localize("sideData." + name);
	}
	
	public boolean hasSlot(int slot)
	{
		for(int i : availableSlots)
		{
			if(i == slot)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static enum EnergyState
	{
		INPUT,
		OUTPUT,
		OFF;
	}
}
