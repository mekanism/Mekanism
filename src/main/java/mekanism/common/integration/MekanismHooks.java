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
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
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
	public static final String COFH_CORE_MOD_ID = "cofhcore";
	public static final String IC2_MOD_ID = "ic2";
	public static final String RAILCRAFT_MOD_ID = "railcraft";
	public static final String THERMALEXPANSION_MOD_ID = "thermalexpansion";
	public static final String COMPUTERCRAFT_MOD_ID = "computercraft";
	public static final String APPLIED_ENERGISTICS_2_MOD_ID = "appliedenergistics2";
	public static final String TESLA_MOD_ID = "tesla";
	public static final String MCMULTIPART_MOD_ID = "mcmultipart";
	public static final String REDSTONEFLUX_MOD_ID = "redstoneflux";
	public static final String METALLURGY_3_CORE_MOD_ID = "Metallurgy3Core";
	public static final String METALLURGY_3_BASE_MOD_ID = "Metallurgy3Base";
	public static final String OPENCOMPUTERS_MOD_ID = "opencomputers";
	public static final String GALACTICRAFT_MOD_ID = "Galacticraft API";
	public static final String WAILA_MOD_ID = "Waila";
	public static final String BUILDCRAFT_MOD_ID = "BuildCraft";

	public boolean IC2Loaded = false;
	public boolean RailcraftLoaded = false;
	public boolean CoFHCoreLoaded = false;
	public boolean TELoaded = false;
	public boolean CCLoaded = false;
	public boolean AE2Loaded = false;
	public boolean TeslaLoaded = false;
	public boolean MCMPLoaded = false;
	public boolean RFLoaded = false;

	public boolean MetallurgyCoreLoaded = false;
	public boolean MetallurgyBaseLoaded = false;

	public void hook()
	{
		if(Loader.isModLoaded(COFH_CORE_MOD_ID)) CoFHCoreLoaded = true;
		if(Loader.isModLoaded(IC2_MOD_ID)) IC2Loaded = true;
		if(Loader.isModLoaded(RAILCRAFT_MOD_ID)) RailcraftLoaded = true;
		if(Loader.isModLoaded(THERMALEXPANSION_MOD_ID)) TELoaded = true;
		if(Loader.isModLoaded(COMPUTERCRAFT_MOD_ID)) CCLoaded = true;
		if(Loader.isModLoaded(APPLIED_ENERGISTICS_2_MOD_ID)) AE2Loaded = true;
		if(Loader.isModLoaded(TESLA_MOD_ID)) TeslaLoaded = true;
		if(Loader.isModLoaded(MCMULTIPART_MOD_ID)) MCMPLoaded = true;
		if(Loader.isModLoaded(REDSTONEFLUX_MOD_ID)) RFLoaded = true;
		
		if(Loader.isModLoaded(METALLURGY_3_CORE_MOD_ID))
		{
			MetallurgyCoreLoaded = true;

			if(Loader.isModLoaded(METALLURGY_3_BASE_MOD_ID)) MetallurgyBaseLoaded = true;
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

		if(Loader.isModLoaded("crafttweaker"))
		{
			CrafttweakerIntegration.apply();
		}
	}

	@Method(modid = MekanismHooks.IC2_MOD_ID)
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

	@Method(modid = COMPUTERCRAFT_MOD_ID)
	public void loadCCPeripheralProviders()
	{
		try {
			ComputerCraftAPI.registerPeripheralProvider(new CCPeripheral.CCPeripheralProvider());
		} catch(Exception e) {}
	}

	@Method(modid = OPENCOMPUTERS_MOD_ID)
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
	
	@Method(modid = APPLIED_ENERGISTICS_2_MOD_ID)
	public void registerAE2P2P() 
	{
		for(TransmitterType type : TransmitterType.values())
		{
			if(type.getTransmission().equals(TransmissionType.ITEM))
			{
				FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-item", new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
				continue;
			}
			
			if(type.getTransmission().equals(TransmissionType.FLUID))
			{
				FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fluid", new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
				continue;
			}
			
			if(type.getTransmission().equals(TransmissionType.ENERGY))
			{
				FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-forge-power", new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
				continue;
			}

		}		
	}
}
