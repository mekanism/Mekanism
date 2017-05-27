package mekanism.common.integration;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.Recipes;

import java.util.Collection;
import java.util.List;

import li.cil.oc.api.Driver;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.Resource;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.integration.computer.CCPeripheral;
import mekanism.common.integration.computer.OCDriver;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.util.MekanismUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import dan200.computercraft.api.ComputerCraftAPI;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 * @author AidanBrady
 *
 */
public final class MekanismHooks
{
	public boolean IC2Loaded = false;
	public boolean RailcraftLoaded = false;
	public boolean CoFHCoreLoaded = false;
	public boolean TELoaded = false;
	public boolean CCLoaded = false;
	public boolean AE2Loaded = false;
	public boolean TeslaLoaded = false;

	public boolean MetallurgyCoreLoaded = false;
	public boolean MetallurgyBaseLoaded = false;

	public void hook()
	{
		if(Loader.isModLoaded("CoFHCore")) CoFHCoreLoaded = true;
		if(Loader.isModLoaded("IC2")) IC2Loaded = true;
		if(Loader.isModLoaded("Railcraft")) RailcraftLoaded = true;
		if(Loader.isModLoaded("ThermalExpansion")) TELoaded = true;
		if(Loader.isModLoaded("ComputerCraft")) CCLoaded = true;
		if(Loader.isModLoaded("appliedenergistics2")) AE2Loaded = true;
		if(Loader.isModLoaded("tesla")) TeslaLoaded = true;
		
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
		for(MachineRecipe<IRecipeInput, Collection<ItemStack>> entry : Recipes.macerator.getRecipes())
		{
			if(!entry.getInput().getInputs().isEmpty())
			{
				List<String> names = MekanismUtils.getOreDictName(entry.getInput().getInputs().get(0));

				for(String name : names)
				{
					boolean did = false;

					if(name.startsWith("ingot"))
					{
						RecipeHandler.addCrusherRecipe(entry.getInput().getInputs().get(0), entry.getOutput().iterator().next());
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
			Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("oreOsmium"), null, false, new ItemStack(MekanismItems.Dust, 2, Resource.OSMIUM.ordinal()));
		} catch(Exception e) {}

		try {
			Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotOsmium"), null, false, new ItemStack(MekanismItems.Dust, 1, Resource.OSMIUM.ordinal()));
			Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotRefinedObsidian"), null, false, new ItemStack(MekanismItems.OtherDust, 1, 5));
			Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotRefinedGlowstone"), null, false, new ItemStack(Items.GLOWSTONE_DUST));
			Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotSteel"), null, false, new ItemStack(MekanismItems.OtherDust, 1, 1));
		} catch(Exception e) {}

		try {
			for(Resource resource : Resource.values())
			{
				Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("clump" + resource.getName()), null, false, new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
			}
		} catch(Exception e) {}
	}

	@Method(modid = "ComputerCraft")
	public void loadCCPeripheralProviders()
	{
		try {
			ComputerCraftAPI.registerPeripheralProvider(new CCPeripheral.CCPeripheralProvider());
		} catch(Exception e) {}
	}

	@Method(modid = "OpenComputers")
	public void loadOCDrivers()
	{
		try {
			Driver.add(new OCDriver());
		} catch(Exception e) {}
	}

	public void addPulverizerRecipe(ItemStack input, ItemStack output, int energy)
	{
		NBTTagCompound nbtTags = new NBTTagCompound();

		nbtTags.setInteger("energy", energy);
		nbtTags.setTag("input", input.writeToNBT(new NBTTagCompound()));
		nbtTags.setTag("primaryOutput", output.writeToNBT(new NBTTagCompound()));

		FMLInterModComms.sendMessage("mekanism", "PulverizerRecipe", nbtTags);
	}
	
	@Method(modid = "appliedenergistics2")
	public void registerAE2P2P() 
	{
		String energyP2P = "add-p2p-attunement-rf-power";
		
		if(IC2Loaded)
		{
			energyP2P = "add-p2p-attunement-ic2-power";
		}
		
		for(TransmitterType type : TransmitterType.values())
		{
			if(type.getTransmission().equals(TransmissionType.ITEM))
			{
				FMLInterModComms.sendMessage("appliedenergistics2", "add-p2p-attunement-item", new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
				continue;
			}
			
			if(type.getTransmission().equals(TransmissionType.FLUID))
			{
				FMLInterModComms.sendMessage("appliedenergistics2", "add-p2p-attunement-fluid", new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
				continue;
			}
			
			if(type.getTransmission().equals(TransmissionType.ENERGY))
			{
				FMLInterModComms.sendMessage("appliedenergistics2", energyP2P, new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
				continue;
			}

		}		
	}
}
