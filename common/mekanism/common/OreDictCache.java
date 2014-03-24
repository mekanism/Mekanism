package mekanism.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.ItemInfo;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictCache
{
	public static HashMap<ItemInfo, List<String>> cachedKeys = new HashMap<ItemInfo, List<String>>();

	public static List<String> getOreDictName(ItemStack check)
	{
		ItemInfo info = ItemInfo.get(check);

		if(cachedKeys.get(info) != null)
		{
			return cachedKeys.get(info);
		}

		List<Integer> idsFound = new ArrayList<Integer>();
		HashMap<Integer, ArrayList<ItemStack>> oreStacks = (HashMap<Integer, ArrayList<ItemStack>>)MekanismUtils.getPrivateValue(null, OreDictionary.class, new String[] {"oreStacks"});
		oreStacks = (HashMap<Integer, ArrayList<ItemStack>>)oreStacks.clone();

		for(Map.Entry<Integer, ArrayList<ItemStack>> entry : oreStacks.entrySet())
		{
			for(ItemStack stack : entry.getValue())
			{
				if(StackUtils.equalsWildcard(stack, check))
				{
					idsFound.add(entry.getKey());
					break;
				}
			}
		}

		List<String> ret = new ArrayList<String>();

		for(Integer id : idsFound)
		{
			ret.add(OreDictionary.getOreName(id));
		}

		cachedKeys.put(info, ret);

		return ret;
	}
}
