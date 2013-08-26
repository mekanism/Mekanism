package mekanism.common;

import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;

import java.util.Map;

import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thermalexpansion.api.crafting.CraftingManagers;
import thermalexpansion.api.crafting.IPulverizerRecipe;
import cpw.mods.fml.common.Loader;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 * @author AidanBrady
 *
 */
public final class MekanismHooks 
{
	private Class BasicComponents;
	
	private Class BuildCraftEnergy;
	
	public boolean IC2Loaded = false;
	public boolean RailcraftLoaded = false;
	public boolean BasicComponentsLoaded = false;
	public boolean BuildCraftLoaded = false;
	public boolean TELoaded = false;
	
	public boolean MetallurgyCoreLoaded = false;
	public boolean MetallurgyBaseLoaded = false;
	
	public void hook()
	{
		if(Loader.isModLoaded("IC2")) IC2Loaded = true;
		if(Loader.isModLoaded("Railcraft")) RailcraftLoaded = true;
		if(Loader.isModLoaded("BasicComponents")) BasicComponentsLoaded = true;
		if(Loader.isModLoaded("BuildCraft|Energy")) BuildCraftLoaded = true;
		if(Loader.isModLoaded("ThermalExpansion")) TELoaded = true;
		
		if(Loader.isModLoaded("Metallurgy3Core"))
		{
			MetallurgyCoreLoaded = true;
			
			if(Loader.isModLoaded("Metallurgy3Base")) MetallurgyBaseLoaded = true;
		}
		
		if(IC2Loaded)
		{
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.OreBlock, 1, 0), null, new ItemStack(Mekanism.Dust, 2, 2));
			
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Ingot, 1, 1), null, new ItemStack(Mekanism.Dust, 1, 2));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Ingot, 1, 0), null, new ItemStack(Mekanism.Dust, 1, 3));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Ingot, 1, 3), null, new ItemStack(Item.glowstone));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Ingot, 1, 4), null, new ItemStack(Mekanism.Dust, 1, 5));
			
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 0), null, new ItemStack(Mekanism.DirtyDust, 1, 0));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 1), null, new ItemStack(Mekanism.DirtyDust, 1, 1));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 2), null, new ItemStack(Mekanism.DirtyDust, 1, 2));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 3), null, new ItemStack(Mekanism.DirtyDust, 1, 3));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 4), null, new ItemStack(Mekanism.DirtyDust, 1, 4));
			Recipes.macerator.addRecipe(new ItemStack(Mekanism.Clump, 1, 5), null, new ItemStack(Mekanism.DirtyDust, 1, 5));
			
			for(Map.Entry<ItemStack, RecipeOutput> entry : Recipes.macerator.getRecipes().entrySet())
			{
				if(MekanismUtils.getName(entry.getKey()).startsWith("ore"))
				{
					if(!Recipe.ENRICHMENT_CHAMBER.containsRecipe(entry.getKey()))
					{
						RecipeHandler.addEnrichmentChamberRecipe(entry.getKey(), entry.getValue().items.get(0));
					}
				}
				else if(MekanismUtils.getName(entry.getKey()).startsWith("ingot"))
				{
					if(!Recipe.CRUSHER.containsRecipe(entry.getKey()))
					{
						RecipeHandler.addCrusherRecipe(entry.getKey(), entry.getValue().items.get(0));
					}
				}
			}
			
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("amplification", 50000);
			
			Recipes.matterAmplifier.addRecipe(new ItemStack(Mekanism.EnrichedAlloy), tag);
			
			System.out.println("[Mekanism] Hooked into IC2 successfully.");
		}
		
		if(BasicComponentsLoaded)
		{
			if(Mekanism.disableBCSteelCrafting)
			{
				MekanismUtils.removeRecipes(getBasicComponentsItem("itemSteelDust"));
				MekanismUtils.removeRecipes(getBasicComponentsItem("itemSteelIngot"));
			}
			
			if(Mekanism.disableBCBronzeCrafting)
			{
				MekanismUtils.removeRecipes(getBasicComponentsItem("itemBronzeDust"));
				MekanismUtils.removeRecipes(getBasicComponentsItem("itemBronzeIngot"));
			}
			
			System.out.println("[Mekanism] Hooked into BasicComponents successfully.");
		}
		
		if(BuildCraftLoaded)
		{
			System.out.println("[Mekanism] Hooked into BuildCraft successfully.");
		}
		
		if(TELoaded)
		{
			for(IPulverizerRecipe recipe : CraftingManagers.pulverizerManager.getRecipeList())
			{
				if(recipe.getSecondaryOutput() == null)
				{
					if(MekanismUtils.getName(recipe.getInput()).startsWith("ore"))
					{
						if(!Recipe.ENRICHMENT_CHAMBER.containsRecipe(recipe.getInput()))
						{
							RecipeHandler.addEnrichmentChamberRecipe(recipe.getInput(), recipe.getPrimaryOutput());
						}
					}
					else if(MekanismUtils.getName(recipe.getInput()).startsWith("ingot"))
					{
						if(!Recipe.CRUSHER.containsRecipe(recipe.getInput()))
						{
							RecipeHandler.addCrusherRecipe(recipe.getInput(), recipe.getPrimaryOutput());
						}
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
