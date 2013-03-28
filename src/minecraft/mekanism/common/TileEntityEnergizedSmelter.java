package mekanism.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class TileEntityEnergizedSmelter extends TileEntityElectricMachine
{
	public TileEntityEnergizedSmelter()
	{
		super("Smelter.ogg", "Energized Smelter", "/mods/mekanism/gui/GuiEnergizedSmelter.png", 10, 200, 2000);
	}
	
	@Override
	public Map getRecipes()
	{
		HashMap<ItemStack, ItemStack> map = new HashMap<ItemStack, ItemStack>();
		
		for(Map.Entry<List<Integer>, ItemStack> entry : FurnaceRecipes.smelting().getMetaSmeltingList().entrySet())
		{
			map.put(new ItemStack(entry.getKey().get(0), 1, entry.getKey().get(1)), entry.getValue());
		}
		
		for(Object obj : FurnaceRecipes.smelting().getSmeltingList().entrySet())
		{
			if(obj instanceof Map.Entry)
			{
				Map.Entry<Integer, ItemStack> entry = (Map.Entry<Integer, ItemStack>)obj;
				map.put(new ItemStack(entry.getKey(), 1, 0), entry.getValue());
			}
		}
		
		return map;
	}
}
