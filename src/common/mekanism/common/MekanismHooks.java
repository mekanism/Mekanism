package mekanism.common;

import cpw.mods.fml.common.Loader;
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
	
	private Class ForestryItem;
	private Class Forestry;
	
	public ItemStack IC2IronDust;
	public ItemStack IC2GoldDust;
	
	public ItemStack RailcraftObsidianDust;
	
	public int BuildCraftFuelID = 3808;
	public ItemStack BuildCraftFuelBucket;
	
	public int ForestryBiofuelID = 5013;
	public ItemStack ForestryBiofuelBucket;
	
	public boolean IC2Loaded = false;
	public boolean RailcraftLoaded = false;
	public boolean BasicComponentsLoaded = false;
	public boolean BuildCraftLoaded = false;
	public boolean ForestryLoaded = false;
	
	public void hook()
	{
		if(Loader.isModLoaded("IC2")) IC2Loaded = true;
		if(Loader.isModLoaded("Railcraft")) RailcraftLoaded = true;
		if(Loader.isModLoaded("BasicComponents")) BasicComponentsLoaded = true;
		if(Loader.isModLoaded("BuildCraft")) BuildCraftLoaded = true;
		if(Loader.isModLoaded("Forestry")) ForestryLoaded = true;
		
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
		if(ForestryLoaded)
		{
			ForestryBiofuelID = getForestryItem("liquidBiofuel").itemID;
			ForestryBiofuelBucket = getForestryItem("bucketBiofuel");
			System.out.println("[Mekanism] Hooked into Forestry successfully.");
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
				throw new Exception();
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
				throw new Exception();
			}
		} catch(Exception e) {
			System.out.println("[Mekanism] Unable to retrieve IC2 item " + name + ".");
			return null;
		}
	}
	
	public ItemStack getForestryItem(String name)
	{
		try {
			if(ForestryItem == null) ForestryItem = Class.forName("forestry.core.config.ForestryItem");
			if(ForestryItem == null) ForestryItem = Class.forName("net.minecraft.src.forestry.core.config.ForestryItem");
			Object ret = ForestryItem.getField(name).get(null);
			
			if(ret instanceof Item)
			{
				return new ItemStack((Item)ret);
			}
			else {
				throw new Exception();
			}
		} catch(Exception e) {
			System.out.println("[Mekanism] Unable to retrieve Forestry item " + name + ".");
			return null;
		}
	}
}
