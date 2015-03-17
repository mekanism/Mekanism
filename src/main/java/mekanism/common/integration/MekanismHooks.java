package mekanism.common.integration;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeInputItemStack;
import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;

import java.util.List;
import java.util.Map;

import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.Resource;
import mekanism.common.block.BlockMachine;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.util.MekanismUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.event.FMLInterModComms;
import dan200.computercraft.api.ComputerCraftAPI;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 * @author AidanBrady
 *
 */
public final class MekanismHooks
{
	private Class BuildCraftEnergy;

	public boolean IC2Loaded = false;
	public boolean RailcraftLoaded = false;
	public boolean CoFHCoreLoaded = false;
	public boolean TELoaded = false;
	public boolean CCLoaded = false;

	public boolean MetallurgyCoreLoaded = false;
	public boolean MetallurgyBaseLoaded = false;

	public void hook()
	{
		if(Loader.isModLoaded("CoFHCore")) CoFHCoreLoaded = true;
		if(Loader.isModLoaded("IC2")) IC2Loaded = true;
		if(Loader.isModLoaded("Railcraft")) RailcraftLoaded = true;
		if(Loader.isModLoaded("ThermalExpansion")) TELoaded = true;
		if(Loader.isModLoaded("ComputerCraft")) CCLoaded = true;

		if(Loader.isModLoaded("Metallurgy3Core"))
		{
			MetallurgyCoreLoaded = true;

			if(Loader.isModLoaded("Metallurgy3Base")) MetallurgyBaseLoaded = true;
		}

		if(IC2Loaded)
		{
			hookIC2Recipes();
			Mekanism.logger.info("Hooked into IC2 successfully.");
		}

		if(CCLoaded)
		{
			loadCCPeripheralProviders();
		}
		
	}

	@Method(modid = "IC2")
	public void hookIC2Recipes()
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
			Recipes.macerator.addRecipe(new RecipeInputOreDict("oreOsmium"), null, new ItemStack(MekanismItems.Dust, 2, Resource.OSMIUM.ordinal()));
		} catch(Exception e) {}

		try {
			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotOsmium"), null, new ItemStack(MekanismItems.Dust, 1, Resource.OSMIUM.ordinal()));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotRefinedObsidian"), null, new ItemStack(MekanismItems.OtherDust, 1, 5));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotRefinedGlowstone"), null, new ItemStack(Items.glowstone_dust));
			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotSteel"), null, new ItemStack(MekanismItems.OtherDust, 1, 1));
		} catch(Exception e) {}

		try {
			for(Resource resource : Resource.values())
			{
				Recipes.macerator.addRecipe(new RecipeInputOreDict("clump" + resource.getName()), null, new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
			}
		} catch(Exception e) {}

		NBTTagCompound tag = new NBTTagCompound();

		tag.setInteger("amplification", 50000);

		Recipes.matterAmplifier.addRecipe(new RecipeInputItemStack(new ItemStack(MekanismItems.EnrichedAlloy), 1), tag);
	}

	@Method(modid = "ComputerCraft")
	public void loadCCPeripheralProviders()
	{
		try {
			ComputerCraftAPI.registerPeripheralProvider((BlockMachine) MekanismBlocks.MachineBlock);
		} catch(Exception ex) {}
	}

	public void addPulverizerRecipe(ItemStack input, ItemStack output, int energy)
	{
		NBTTagCompound nbtTags = new NBTTagCompound();

		nbtTags.setInteger("energy", energy);
		nbtTags.setTag("input", input.writeToNBT(new NBTTagCompound()));
		nbtTags.setTag("primaryOutput", output.writeToNBT(new NBTTagCompound()));

		FMLInterModComms.sendMessage("mekanism", "PulverizerRecipe", nbtTags);
	}
}
