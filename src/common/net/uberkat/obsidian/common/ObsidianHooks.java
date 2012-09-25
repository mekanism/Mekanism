package net.uberkat.obsidian.common;

import ic2.api.Ic2Recipes;
import net.minecraft.src.*;

/**
 * Hooks for Obsidian Ingots. Use to grab items or blocks out of different mods.
 * @author AidanBrady
 *
 */
public class ObsidianHooks 
{
	private Class Ic2Items;
	
	public ItemStack IC2IronDust;
	public ItemStack IC2GoldDust;
	
	public boolean IC2Loaded;
	
	public void hook()
	{
		if(getIC2Item("uraniumOre", true) != null) IC2Loaded = true;
		
		if(IC2Loaded)
		{
			IC2IronDust = getIC2Item("ironDust", false);
			IC2GoldDust = getIC2Item("goldDust", false);
			
			Ic2Recipes.addMaceratorRecipe(new ItemStack(ObsidianIngots.MultiBlock, 1, 0), new ItemStack(ObsidianIngots.PlatinumDust, 2));
		}
	}
	
	/**
	 * Gets an object out of the class Ic2Items.
	 * @param name - name of the item
	 * @param test - whether or not this is a test
	 * @return the object
	 */
	public ItemStack getIC2Item(String name, boolean test)
	{
		try {
			if(Ic2Items == null) Ic2Items = Class.forName("ic2.common.Ic2Items");
			if(Ic2Items == null) Ic2Items = Class.forName("net.minecraft.src.ic2.common.Ic2Items");
			Object ret = Ic2Items.getField(name).get(null);
			
			if(ret instanceof ItemStack)
			{
				return (ItemStack)ret;
			}
			else {
				return null;
			}
		} catch(Exception e) {
			if(!test)
			{
				System.out.println("[ObsidianIngots] Unable to retrieve IC2 item " + name + ".");
			}
			else {
				System.out.println("[ObsidianIngots] Unable to hook into IC2.");
			}
			return null;
		}
	}
}
