package mekanism.common;

import ic2.api.recipe.Recipes;

import java.util.Map;

import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thermalexpansion.api.crafting.CraftingManagers;
import thermalexpansion.api.crafting.IPulverizerRecipe;
import universalelectricity.prefab.RecipeHelper;
import cpw.mods.fml.common.Loader;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 * @author AidanBrady
 *
 */
public final class MekanismHooks 
{
	private Class IC2;
	
	private Class Railcraft;
	
	private Class BasicComponents;
	
	private Class BuildCraftEnergy;
	
	private Class ForestryItem;
	private Class Forestry;
	
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
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.OreBlock, 1, 0), new ItemStack(Mekanism.Dust, 2, 2));
			
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Ingot, 1, 1), new ItemStack(Mekanism.Dust, 1, 2));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Ingot, 1, 0), new ItemStack(Mekanism.Dust, 1, 3));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Ingot, 1, 3), new ItemStack(Item.lightStoneDust));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Ingot, 1, 4), new ItemStack(Mekanism.Dust, 1, 5));
			
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 0), new ItemStack(Mekanism.DirtyDust, 1, 0));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 1), new ItemStack(Mekanism.DirtyDust, 1, 1));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 2), new ItemStack(Mekanism.DirtyDust, 1, 2));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 3), new ItemStack(Mekanism.DirtyDust, 1, 3));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 4), new ItemStack(Mekanism.DirtyDust, 1, 4));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 5), new ItemStack(Mekanism.DirtyDust, 1, 5));
			
			for(Map.Entry<ItemStack, ItemStack> entry : Recipes.macerator.getRecipes().entrySet())
			{
				if(!Recipe.ENRICHMENT_CHAMBER.get().containsKey(entry.getKey()))
				{
					RecipeHandler.addEnrichmentChamberRecipe(entry.getKey(), entry.getValue());
				}
			}
			
			Recipes.matterAmplifier.addRecipe(new ItemStack(Mekanism.EnrichedAlloy), 50000);
			
			System.out.println("[Mekanism] Hooked into IC2 successfully.");
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
		if(TELoaded)
		{
			for(IPulverizerRecipe recipe : CraftingManagers.pulverizerManager.getRecipeList())
			{
				if(recipe.getSecondaryOutput() == null)
				{
					if(!Recipe.ENRICHMENT_CHAMBER.get().containsKey(recipe.getInput()))
					{
						RecipeHandler.addEnrichmentChamberRecipe(recipe.getInput(), recipe.getPrimaryOutput());
					}
				}
			}
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
