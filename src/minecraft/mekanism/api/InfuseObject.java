package mekanism.api;

public class InfuseObject 
{
	/** The type of infuse this item stores */
	public InfusionType type;
	
	/** How much infuse this item stores */
	public int stored;
	
	public InfuseObject(InfusionType infusion, int i)
	{
		type = infusion;
		stored = i;
	}
}
