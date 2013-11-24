package mekanism.api.gas;

import net.minecraft.util.Icon;

/**
 * The gasses currently available in Mekanism.
 * @author AidanBrady
 *
 */
public enum EnumGas 
{
	NONE("None", null),
	OXYGEN("Oxygen", null),
	HYDROGEN("Hydrogen", null);
	
	public String name;
	public Icon gasIcon;
	
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
	
	public boolean hasTexture()
	{
		return gasIcon != null;
	}
	
	private EnumGas(String s, Icon icon)
	{
		name = s;
		gasIcon = icon;
	}
}
