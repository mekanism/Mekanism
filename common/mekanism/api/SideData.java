package mekanism.api;

public class SideData 
{
	/** The color of this SideData */
	public EnumColor color;
	
	/** Int[] of available side slots */
	public int[] availableSlots;
	
	public SideData(EnumColor colour, int[] slots)
	{
		color = colour;
		availableSlots = slots;
	}
}
