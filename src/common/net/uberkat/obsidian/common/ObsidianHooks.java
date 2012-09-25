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
	private Class IC2;
	
	public ItemStack IC2IronDust;
	public ItemStack IC2GoldDust;
	
	public boolean IC2Loaded = false;
	
	public void hook()
	{
		if(isIC2Installed()) IC2Loaded = true;
		
		if(IC2Loaded)
		{
			IC2IronDust = getIC2Item("ironDust");
			IC2GoldDust = getIC2Item("goldDust");
			
			Ic2Recipes.addMaceratorRecipe(new ItemStack(ObsidianIngots.MultiBlock, 1, 0), new ItemStack(ObsidianIngots.PlatinumDust, 2));
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Block.obsidian), new ItemStack(ObsidianIngots.ObsidianDust));
			Ic2Recipes.addMatterAmplifier(ObsidianIngots.EnrichedAlloy, 100000);
			
			System.out.println("[ObsidianIngots] Hooked into IC2 successfully.");
		}
	}
	
	/**
	 * Gets an object out of the class Ic2Items.
	 * @param name - name of the item
	 * @param test - whether or not this is a test
	 * @return the object
	 */
	public ItemStack getIC2Item(String name)
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
			System.out.println("[ObsidianIngots] Unable to retrieve IC2 item " + name + ".");
			return null;
		}
	}
	
	public boolean isIC2Installed()
	{
		try {
			if(IC2 == null) IC2 = Class.forName("ic2.common.IC2");
			if(IC2 == null) IC2 = Class.forName("net.minecraft.src.ic2.common.IC2");
			Object ret = IC2.getField("platform").get(null);
			
			if(ret != null)
			{
				return true;
			}
			return false;
		} catch(Exception e) {
			System.out.println("[ObsidianIngots] Unable to hook into IC2.");
			return false;
		}
	}
}
