package mekanism.common;

public enum Resource
{
	IRON("Iron", 0xccccd9),
	GOLD("Gold", 0xf2cd67),
	OSMIUM("Osmium", 0x1e79c3),
	COPPER("Copper", 0xaa4b19),
	TIN("Tin", 0xccccd9),
	SILVER("Silver", 0xccccd9),
	LEAD("Lead", 0x3d3d41);

	private String name;
	public final int tint;

	Resource(String s, int t)
	{
		name = s;
		tint = t;
	}

	public static Resource getFromName(String s)
	{
		for(Resource r : values())
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
