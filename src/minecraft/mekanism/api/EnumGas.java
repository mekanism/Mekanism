package mekanism.api;

import net.minecraft.item.Item;

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
	public Item gasItem;
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
		return gasItem != null && texturePath != null;
	}
	
	private EnumGas(String s, Item item, String path)
	{
		name = s;
		gasItem = item;
		texturePath = path;
	}
}
