package mekanism.api;

/**
 * The types of infuse currently available in Mekanism.
 * @author AidanBrady
 *
 */
public enum InfusionType 
{
	COAL("COAL"),
	TIN("TIN"),
	DIAMOND("DIAMOND"),
	BIO("BIO"),
	NONE("NONE");
	
	/** The name of this infusion */
	public String name;
	
	public static InfusionType getFromName(String infusionName)
	{
		for(InfusionType type : values())
		{
			if(infusionName.contains(type.name))
			{
				return type;
			}
		}
		
		System.out.println("[Mekanism] Invalid infusion identifier when retrieving with name.");
		return NONE;
	}
	
	private InfusionType(String s)
	{
		name = s;
	}
}
