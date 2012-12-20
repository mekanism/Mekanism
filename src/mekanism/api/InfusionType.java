package mekanism.api;

import mekanism.api.Tier.EnergyCubeTier;

public enum InfusionType 
{
	COAL("COAL"),
	TIN("TIN"),
	NONE("NONE");
	
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
		
		System.out.println("[Mekanism] Invalid tier identifier when retrieving with name.");
		return NONE;
	}
	
	private InfusionType(String s)
	{
		name = s;
	}
}
