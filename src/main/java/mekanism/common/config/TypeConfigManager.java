package mekanism.common.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.common.base.IBlockType;
import net.minecraft.item.crafting.CraftingManager;

public class TypeConfigManager 
{
	private Map<String, Boolean> config = new HashMap<String, Boolean>();
	
	public boolean isEnabled(String type)
	{
		return config.get(type) != null && config.get(type);
	}
	
	public void setEntry(String type, boolean enabled)
	{
		config.put(type, enabled);
	}
	
	public static void updateConfigRecipes(List blocks, TypeConfigManager manager)
	{
		for(Object obj : blocks) //enums are quirky
		{
			IBlockType type = (IBlockType)obj;
			
			if(manager.isEnabled(type.getBlockName()))
			{
				CraftingManager.getInstance().getRecipeList().removeAll(type.getRecipes());
				CraftingManager.getInstance().getRecipeList().addAll(type.getRecipes());
			}
			else {
				CraftingManager.getInstance().getRecipeList().removeAll(type.getRecipes());
			}
		}
	}
}
