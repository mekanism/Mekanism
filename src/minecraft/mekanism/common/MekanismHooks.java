package mekanism.common;

import universalelectricity.prefab.RecipeHelper;
import cpw.mods.fml.common.Loader;
import ic2.api.Ic2Recipes;
import ic2.api.Items;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.*;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

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
	
	private Class BasicComponents;
	
	private Class BuildCraftEnergy;
	
	private Class ForestryItem;
	private Class Forestry;
	
	public ItemStack IC2TinDust;
	public ItemStack IC2CoalDust;
	
	public int BuildCraftFuelID = 19108;
	public ItemStack BuildCraftFuelBucket;
	
	public int BuildCraftOilID = 1521;
	public ItemStack BuildCraftOilBucket;
	
	public int ForestryBiofuelID = 5013;
	public ItemStack ForestryBiofuelBucket;
	
	public boolean IC2Loaded = false;
	public boolean RailcraftLoaded = false;
	public boolean BasicComponentsLoaded = false;
	public boolean BuildCraftLoaded = false;
	public boolean ForestryLoaded = false;
	public boolean TELoaded = false;
	
	public void hook()
	{
		if(Loader.isModLoaded("IC2")) IC2Loaded = true;
		if(Loader.isModLoaded("Railcraft")) RailcraftLoaded = true;
		if(Loader.isModLoaded("BasicComponents")) BasicComponentsLoaded = true;
		if(Loader.isModLoaded("BuildCraft|Energy")) BuildCraftLoaded = true;
		if(Loader.isModLoaded("Forestry")) ForestryLoaded = true;
		if(Loader.isModLoaded("ThermalExpansion")) TELoaded = true;
		
		if(IC2Loaded)
		{
			IC2TinDust = getIC2Item("tinDust");
			IC2CoalDust = getIC2Item("coalDust");
			
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.OreBlock, 1, 0), new ItemStack(Mekanism.Dust, 2, 2));
			
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.Ingot, 1, 1), new ItemStack(Mekanism.Dust, 1, 2));
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.Ingot, 1, 0), new ItemStack(Mekanism.Dust, 1, 3));
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.Ingot, 1, 3), new ItemStack(Item.lightStoneDust));
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.Ingot, 1, 4), new ItemStack(Mekanism.Dust, 1, 5));
			
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.Clump, 1, 0), new ItemStack(Mekanism.DirtyDust, 1, 0));
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.Clump, 1, 1), new ItemStack(Mekanism.DirtyDust, 1, 1));
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.Clump, 1, 2), new ItemStack(Mekanism.DirtyDust, 1, 2));
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.Clump, 1, 3), new ItemStack(Mekanism.DirtyDust, 1, 3));
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.Clump, 1, 4), new ItemStack(Mekanism.DirtyDust, 1, 4));
			Ic2Recipes.addMaceratorRecipe(new ItemStack(Mekanism.Clump, 1, 5), new ItemStack(Mekanism.DirtyDust, 1, 5));
			
			Ic2Recipes.addMatterAmplifier(Mekanism.EnrichedAlloy, 50000);
			
			System.out.println("[Mekanism] Hooked into IC2 successfully.");
		}
		if(RailcraftLoaded)
		{
			System.out.println("[Mekanism] Hooked into Railcraft successfully.");
		}
		if(BasicComponentsLoaded)
		{
			if(Mekanism.disableBCSteelCrafting)
			{
				RecipeHelper.removeRecipes(getBasicComponentsItem("itemSteelDust"));
				RecipeHelper.removeRecipes(getBasicComponentsItem("itemSteelIngot"));
			}
			
			if(Mekanism.disableBCBronzeCrafting)
			{
				RecipeHelper.removeRecipes(getBasicComponentsItem("itemBronzeDust"));
				RecipeHelper.removeRecipes(getBasicComponentsItem("itemBronzeIngot"));
			}
			
			System.out.println("[Mekanism] Hooked into BasicComponents successfully.");
		}
		if(BuildCraftLoaded)
		{
			BuildCraftFuelID = getBuildCraftItem("fuel").itemID;
			BuildCraftFuelBucket = getBuildCraftItem("bucketFuel");
			BuildCraftOilID = getBuildCraftItem("oilStill").itemID;
			BuildCraftOilBucket = getBuildCraftItem("bucketOil");
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
	 * @return the object
	 */
	public ItemStack getIC2Item(String name)
	{
		try {
			if(Ic2Items == null) Ic2Items = Class.forName("ic2.core.Ic2Items");
			if(Ic2Items == null) Ic2Items = Class.forName("net.minecraft.src.ic2.core.Ic2Items");
			Object ret = Ic2Items.getField(name).get(null);
			
			if(ret instanceof ItemStack)
			{
				return (ItemStack)ret;
			}
			else if(ret instanceof Block)
			{
				return new ItemStack((Block)ret);
			}
			else {
				throw new Exception("not instanceof ItemStack");
			}
		} catch(Exception e) {
			System.out.println("[Mekanism] Unable to retrieve IC2 item " + name + ".");
			return null;
		}
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
			else if(ret instanceof Block)
			{
				return new ItemStack((Block)ret);
			}
			else {
				throw new Exception("not instanceof ItemStack");
			}
		} catch(Exception e) {
			System.out.println("[Mekanism] Unable to retrieve BuildCraft item " + name + ".");
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
			else if(ret instanceof Block)
			{
				return new ItemStack((Block)ret);
			}
			else {
				throw new Exception("not instanceof ItemStack");
			}
		} catch(Exception e) {
			System.out.println("[Mekanism] Unable to retrieve Forestry item " + name + ".");
			return null;
		}
	}
	
	public ItemStack getBasicComponentsItem(String name)
	{
		try {
			if(BasicComponents == null) BasicComponents = Class.forName("basiccomponents.common.BasicComponents");
			if(BasicComponents == null) BasicComponents = Class.forName("net.minecraft.src.basiccomponents.common.BasicComponents");
			Object ret = BasicComponents.getField(name).get(null);
			
			if(ret instanceof Item)
			{
				return new ItemStack((Item)ret);
			}
			else if(ret instanceof Block)
			{
				return new ItemStack((Block)ret);
			}
			else {
				throw new Exception("not instanceof ItemStack");
			}
		} catch(Exception e) {
			System.out.println("[Mekanism] Unable to retrieve Basic Components item " + name + ".");
			return null;
		}
	}
}
