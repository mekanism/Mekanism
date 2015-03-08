package mekanism.common;

import mekanism.api.EnumColor;

public class SideData
{
	/** The color of this SideData */
	public EnumColor color;

	/** Int[] of available side slots */
	public int[] availableSlots;
	
	/** EnergyState representing this SideData */
	public EnergyState energyState;

	public SideData(EnumColor colour, int[] slots)
	{
		color = colour;
		availableSlots = slots;
	}
	
	public SideData(EnumColor colour, EnergyState state)
	{
		color = colour;
		energyState = state;
	}
	
	public static enum EnergyState
	{
		INPUT,
		OUTPUT,
		OFF;
	}
}
