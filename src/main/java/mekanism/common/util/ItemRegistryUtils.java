package mekanism.common.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import org.apache.commons.lang3.text.WordUtils;

public final class ItemRegistryUtils 
{
	private static final Map<String, String> modIDMap = new HashMap<String, String>();
	
	private static void populateMap()
	{
		for(Map.Entry<String, ModContainer> entry : Loader.instance().getIndexedModList().entrySet()) 
		{
			modIDMap.put(entry.getKey().toLowerCase(), entry.getValue().getName());
		}
	}
	
	/* Mod ID lookup thanks to JEI */
	public static String getMod(ItemStack stack)
	{
		if(stack == null || stack.getItem() == null)
		{
			return "null";
		}
		
		if(modIDMap.isEmpty())
		{
			populateMap();
		}
		
		ResourceLocation itemResourceLocation = Item.itemRegistry.getNameForObject(stack.getItem());
		String modId = itemResourceLocation.getResourceDomain();
		String lowercaseModId = modId.toLowerCase(Locale.ENGLISH);
		String modName = modIDMap.get(lowercaseModId);
		
		if(modName == null) 
		{
			modName = WordUtils.capitalize(modId);
			modIDMap.put(lowercaseModId, modName);
		}
		
		return modName;
	}
}
