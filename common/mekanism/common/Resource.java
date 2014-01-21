package mekanism.common;

public enum Resource
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
	
	private Resource(String s)
	{
		name = s;
	}
	
	public String getName()
	{
		return name;
	}
}
