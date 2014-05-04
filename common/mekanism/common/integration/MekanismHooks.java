package mekanism.common.integration;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeInputItemStack;
import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;

import java.util.List;
import java.util.Map;

import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import dan200.computercraft.api.ComputerCraftAPI;

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
	public boolean CCLoaded = false;

	public boolean MetallurgyCoreLoaded = false;
	public boolean MetallurgyBaseLoaded = false;

	public void hook()
	{
		if(Loader.isModLoaded("IC2")) IC2Loaded = true;
		if(Loader.isModLoaded("Railcraft")) RailcraftLoaded = true;
		if(Loader.isModLoaded("BasicComponents")) BasicComponentsLoaded = true;
		if(Loader.isModLoaded("BuildCraft|Energy")) BuildCraftLoaded = true;
		if(Loader.isModLoaded("ThermalExpansion")) TELoaded = true;
		if(Loader.isModLoaded("ComputerCraft")) CCLoaded = true;

		if(Loader.isModLoaded("Metallurgy3Core"))
		{
			MetallurgyCoreLoaded = true;

			if(Loader.isModLoaded("Metallurgy3Base")) MetallurgyBaseLoaded = true;
		}

		if(IC2Loaded)
		{
			for(Map.Entry<IRecipeInput, RecipeOutput> entry : Recipes.macerator.getRecipes().entrySet())
			{
				if(!entry.getKey().getInputs().isEmpty())
				{
					List<String> names = MekanismUtils.getOreDictName(entry.getKey().getInputs().get(0));

					for(String name : names)
					{
						boolean did = false;

						if(name.startsWith("ingot"))
						{
							RecipeHandler.addCrusherRecipe(entry.getKey().getInputs().get(0), entry.getValue().items.get(0));
							did = true;
						}

						if(did)
						{
							break;
						}
					}
				}
			}

			try {
				Recipes.macerator.addRecipe(new RecipeInputOreDict("oreOsmium"), null, new ItemStack(Mekanism.Dust, 2, 2));
			} catch(Exception e) {}

			try {
				Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotOsmium"), null, new ItemStack(Mekanism.Dust, 1, 2));
				Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotRefinedObsidian"), null, new ItemStack(Mekanism.Dust, 1, 3));
				Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotRefinedGlowstone"), null, new ItemStack(Item.glowstone));
				Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotSteel"), null, new ItemStack(Mekanism.Dust, 1, 5));
			} catch(Exception e) {}

			try {
				Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpIron"), null, new ItemStack(Mekanism.DirtyDust, 1, 0));
				Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpGold"), null, new ItemStack(Mekanism.DirtyDust, 1, 1));
				Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpOsmium"), null, new ItemStack(Mekanism.DirtyDust, 1, 2));
				Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpCopper"), null, new ItemStack(Mekanism.DirtyDust, 1, 3));
				Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpTin"), null, new ItemStack(Mekanism.DirtyDust, 1, 4));
				Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpSilver"), null, new ItemStack(Mekanism.DirtyDust, 1, 5));
				Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpObsidian"), null, new ItemStack(Mekanism.DirtyDust, 1, 6));
				Recipes.macerator.addRecipe(new RecipeInputOreDict("clumpLead"), null, new ItemStack(Mekanism.DirtyDust, 1, 7));
			} catch(Exception e) {}

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
		
		if(CCLoaded)
		{
			try {
				ComputerCraftAPI.registerPeripheralProvider((BlockMachine)Mekanism.MachineBlock);
			} catch(Exception ex) {}
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
}
