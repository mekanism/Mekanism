package mekanism.api;

public class SideData 
{
	/** The color of this SideData */
	public EnumColor color;
	
	/** When the side's slot IDs start */
	public int slotStart;
	
	/** How many slots this side contains */
	public int slotAmount;
	
	/** Int[] of available side slots */
	public int[] availableSlots;
	
	public SideData(EnumColor colour, int start, int amount, int[] slots)
	{
		color = colour;
		slotStart = start;
		slotAmount = amount;
		availableSlots = slots;
	}
}
