package mekanism.common;

import railcraft.common.api.core.items.ItemRegistry;
import ic2.api.Ic2Recipes;
import net.minecraft.src.*;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 * @author AidanBrady
 *
 */
public class MekanismHooks 
{
	private Class Ic2Items;
	private Class IC2;
	
	private Class Railcraft;
	
	private Class BCLoader;
	
	public ItemStack IC2IronDust;
	public ItemStack IC2GoldDust;
	
	public ItemStack RailcraftObsidianDust;
	
	public boolean IC2Loaded = false;
	public boolean RailcraftLoaded = false;
	public boolean BCLoaded = false;
	
	public void hook()
	{
		if(isIC2Installed()) IC2Loaded = true;
		if(isRailcraftInstalled()) RailcraftLoaded = true;
		if(isBCInstalled()) BCLoaded = true;
		
		if(IC2Loaded)
		{
			IC2IronDust = getIC2Item("ironDust");
			IC2GoldDust = getIC2Item("goldDust");
			
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.OreBlock, 1, 0), new ItemStack(Mekanism.Dust, 2, 2));
			Ic2Recipes.addMatterAmplifier(Mekanism.EnrichedAlloy, 100000);
			
			System.out.println("[Mekanism] Hooked into IC2 successfully.");
		}
		if(RailcraftLoaded)
		{
			RailcraftObsidianDust = getRailcraftItem("dust.obsidian");
			
			System.out.println("[Mekanism] Hooked into Railcraft successfully.");
		}
		if(BCLoaded)
		{
			System.out.println("[Mekanism] Hooked into BasicComponents successfully.");
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
			System.out.println("[Mekanism] Unable to retrieve IC2 item " + name + ".");
			return null;
		}
	}
	
	public ItemStack getRailcraftItem(String name)
	{
		return ItemRegistry.getItem(name, 1);
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
			System.out.println("[Mekanism] Unable to hook into IC2.");
			return false;
		}
	}
	
	public boolean isRailcraftInstalled()
	{
		try {
			if(Railcraft == null) Railcraft = Class.forName("railcraft.common.core.Railcraft");
			Object ret = Railcraft.getField("instance").get(null);
			
			if(ret != null)
			{
				return true;
			}
			return false;
		} catch(Exception e) {
			System.out.println("[Mekanism] Unable to hook into Railcraft.");
			return false;
		}
	}
	
	public boolean isBCInstalled()
	{
		try {
			if(BCLoader == null) BCLoader = Class.forName("basiccomponents.BCLoader");
			Object ret = BCLoader.getField("instance").get(null);
			
			if(ret != null)
			{
				return true;
			}
			return false;
		} catch(Exception e) {
			System.out.println("[Mekanism] Unable to hook into BasicComponents.");
			return false;
		}
	}
}
