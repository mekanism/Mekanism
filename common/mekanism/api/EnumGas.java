package mekanism.api;

import net.minecraft.util.Icon;

/**
 * The gasses currently available in Mekanism.
 * @author AidanBrady
 *
 */
public enum EnumGas 
{
	NONE("None", null, null),
	OXYGEN("Oxygen", null, null),
	HYDROGEN("Hydrogen", null, null);
	
	public String name;
	public Icon gasIcon;
	public String texturePath;
	
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
		return gasIcon != null && texturePath != null;
	}
	
	private EnumGas(String s, Icon icon, String path)
	{
		name = s;
		gasIcon = icon;
		texturePath = path;
	}
}
