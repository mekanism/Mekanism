package mekanism.api;


public class SideData 
{
	/** The color of this SideData */
	public EnumColor color;
	
	/** When the side's slot IDs start */
	public int slotStart;
	
	/** How many slots this side contains */
	public int slotAmount;
	
	public SideData(EnumColor colour, int start, int amount)
	{
		color = colour;
		slotStart = start;
		slotAmount = amount;
	}
}
