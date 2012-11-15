package mekanism.common;

import railcraft.common.api.core.items.ItemRegistry;
import ic2.api.Ic2Recipes;
import net.minecraft.src.*;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 * @author AidanBrady
 *
 */
public final class MekanismHooks 
{
	private Class Ic2Items;
	private Class IC2;
	
	private Class Railcraft;
	
	private Class BCLoader;
	
	private Class BuildCraftEnergy;
	
	public ItemStack IC2IronDust;
	public ItemStack IC2GoldDust;
	
	public ItemStack RailcraftObsidianDust;
	
	public int BuildCraftFuelID = 3808;
	public ItemStack BuildCraftFuelBucket;
	
	public boolean IC2Loaded = false;
	public boolean RailcraftLoaded = false;
	public boolean BasicComponentsLoaded = false;
	public boolean BuildCraftLoaded = false;
	
	public void hook()
	{
		if(isIC2Installed()) IC2Loaded = true;
		if(isRailcraftInstalled()) RailcraftLoaded = true;
		if(isBasicComponentsInstalled()) BasicComponentsLoaded = true;
		if(isBuildCraftInstalled()) BuildCraftLoaded = true;
		
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
		if(BasicComponentsLoaded)
		{
			System.out.println("[Mekanism] Hooked into BasicComponents successfully.");
		}
		if(BuildCraftLoaded)
		{
			BuildCraftFuelID = getBuildCraftItem("fuel").itemID;
			BuildCraftFuelBucket = getBuildCraftItem("bucketFuel");
			System.out.println("[Mekanism] Hooked into BuildCraft successfully.");
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
	
	public ItemStack getBuildCraftItem(String name)
	{
		try {
			if(BuildCraftEnergy == null) BuildCraftEnergy = Class.forName("buildcraft.BuildCraftEnergy");
			if(BuildCraftEnergy == null) BuildCraftEnergy = Class.forName("net.minecraft.src.buildcraft.BuildCraftEnergy");
			Object ret = BuildCraftEnergy.getField(name).get(null);
			
			if(ret instanceof Item)
			{
				return new ItemStack((Item)ret);
			}
			else {
				return null;
			}
		} catch(Exception e) {
			System.out.println("[Mekanism] Unable to retrieve IC2 item " + name + ".");
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
			System.out.println("[Mekanism] Unable to hook into IC2.");
			return false;
		}
	}
	
	public boolean isRailcraftInstalled()
	{
		try {
			if(Railcraft == null) Railcraft = Class.forName("railcraft.common.core.Railcraft");
			if(Railcraft == null) Railcraft = Class.forName("net.minecraft.src.railcraft.common.core.Railcraft");
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
	
	public boolean isBasicComponentsInstalled()
	{
		try {
			if(BCLoader == null) BCLoader = Class.forName("basiccomponents.BCLoader");
			if(BCLoader == null) BCLoader = Class.forName("net.minecraft.src.basiccomponents.BCLoader");
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
	
	public boolean isBuildCraftInstalled()
	{
		try {
			if(BuildCraftEnergy == null) BuildCraftEnergy = Class.forName("buildcraft.BuildCraftEnergy");
			if(BuildCraftEnergy == null) BuildCraftEnergy = Class.forName("net.minecraft.src.buildcraft.BuildCraftEnergy");
			Object ret = BuildCraftEnergy.getField("instance").get(null);
			
			if(ret != null)
			{
				return true;
			}
			return false;
		} catch(Exception e) {
			System.out.println("[Mekanism] Unable to hook into BuildCraft.");
			return false;
		}
	}
}
