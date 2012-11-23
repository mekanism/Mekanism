package mekanism.api;

/**
 * 
 * @author AidanBrady
 *
 */
public enum EnumGas 
{
	NONE("None"),
	OXYGEN("Oxygen"),
	HYDROGEN("Hydrogen");
	
	public String name;
	
	public static EnumGas getFromName(String gasName)
	{
		for(EnumGas gas : values())
		{
			if(gasName.contains(gas.name))
			{
				return gas;
			}
		}
		
		System.out.println("[Mekanism] Invalid gas identifier when retrieving with name.");
		return NONE;
	}
	
	private EnumGas(String s)
	{
		name = s;
	}
}
