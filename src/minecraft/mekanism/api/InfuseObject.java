package mekanism.api;

/**
 * InfuseObject - an object associated with an ItemStack that can modify a Metallurgic Infuser's internal infuse.
 * @author AidanBrady
 *
 */
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
