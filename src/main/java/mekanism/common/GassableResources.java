package mekanism.common;

public enum GassableResources
{
	IRON("Iron"),
	GOLD("Gold"),
	OSMIUM("Osmium"),
	COPPER("Copper"),
	TIN("Tin"),
	SILVER("Silver"),
	OBSIDIAN("Obsidian"),
	LEAD("Lead");

	private String name;

	private GassableResources(String s)
	{
		name = s;
	}

	public static GassableResources getFromName(String s)
	{
		for(GassableResources r : values())
		{
			if(r.name.toLowerCase().equals(s.toLowerCase()))
			{
				return r;
			}
		}

		return null;
	}

	public String getName()
	{
		return name;
	}
}
