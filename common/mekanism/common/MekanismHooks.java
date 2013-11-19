package mekanism.common;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeInputItemStack;
import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;

import java.util.Map;

import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

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
			Recipes.macerator.addRecipe(new RecipeInputOreDict("oreOsmium"), null, new ItemStack(Mekanism.Dust, 2, 2));

			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotOsmium"), null, new ItemStack(Mekanism.Dust, 1, 2));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotRefinedObsidian"), null, new ItemStack(Mekanism.Dust, 1, 3));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotRefinedGlowstone"), null, new ItemStack(Item.glowstone));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotSteel"), null, new ItemStack(Mekanism.Dust, 1, 5));
			
			Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpIron"), null, new ItemStack(Mekanism.DirtyDust, 1, 0));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpGold"), null, new ItemStack(Mekanism.DirtyDust, 1, 1));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpOsmium"), null, new ItemStack(Mekanism.DirtyDust, 1, 2));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpCopper"), null, new ItemStack(Mekanism.DirtyDust, 1, 3));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpTin"), null, new ItemStack(Mekanism.DirtyDust, 1, 4));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpSilver"), null, new ItemStack(Mekanism.DirtyDust, 1, 5));
			
			for(Map.Entry<IRecipeInput, RecipeOutput> entry : Recipes.macerator.getRecipes().entrySet())
			{
				if(!entry.getKey().getInputs().isEmpty())
				{
					if(MekanismUtils.getName(entry.getKey().getInputs().get(0)).startsWith("ore"))
					{
						if(!Recipe.ENRICHMENT_CHAMBER.containsRecipe(entry.getKey().getInputs().get(0)))
						{
							RecipeHandler.addEnrichmentChamberRecipe(entry.getKey().getInputs().get(0), entry.getValue().items.get(0));
						}
					}
					else if(MekanismUtils.getName(entry.getKey().getInputs().get(0)).startsWith("ingot"))
					{
						if(!Recipe.CRUSHER.containsRecipe(entry.getKey().getInputs().get(0)))
						{
							RecipeHandler.addCrusherRecipe(entry.getKey().getInputs().get(0), entry.getValue().items.get(0));
						}
					}
				}
			}
			
			NBTTagCompound tag = new NBTTagCompound();
			
			tag.setInteger("amplification", 50000);
			
			Recipes.matterAmplifier.addRecipe(new RecipeInputItemStack(new ItemStack(Mekanism.EnrichedAlloy), 1), tag);
			
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
	}
	
	public void addPulverizerRecipe(ItemStack input, ItemStack output, int energy)
	{
		NBTTagCompound nbtTags = new NBTTagCompound();
		
		nbtTags.setInteger("energy", energy);
		nbtTags.setCompoundTag("input", input.writeToNBT(new NBTTagCompound()));
		nbtTags.setCompoundTag("primaryOutput", output.writeToNBT(new NBTTagCompound()));
		
		FMLInterModComms.sendMessage("mekanism", "PulverizerRecipe", nbtTags);
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
	
	@EventHandler
	public void handleIMC(IMCEvent event)
	{
		for(IMCMessage message : event.getMessages())
		{
			try {
				if(message.isNBTMessage())
				{
					if(message.key.equalsIgnoreCase("PulverizerRecipe") && !message.getNBTValue().hasKey("secondaryChance") && !message.getNBTValue().hasKey("secondaryOutput"))
					{
						ItemStack input = ItemStack.loadItemStackFromNBT(message.getNBTValue().getCompoundTag("input"));
						ItemStack output = ItemStack.loadItemStackFromNBT(message.getNBTValue().getCompoundTag("output"));
						
						if(input != null && output != null)
						{
							if(MekanismUtils.getName(input).startsWith("ore"))
							{
								if(!Recipe.ENRICHMENT_CHAMBER.containsRecipe(input))
								{
									RecipeHandler.addEnrichmentChamberRecipe(input, output);
								}
							}
							else if(MekanismUtils.getName(input).startsWith("ingot"))
							{
								if(!Recipe.CRUSHER.containsRecipe(input))
								{
									RecipeHandler.addCrusherRecipe(input, output);
								}
							}
						}
					}
				}
			} catch(Exception e) {}
		}
	}
}
