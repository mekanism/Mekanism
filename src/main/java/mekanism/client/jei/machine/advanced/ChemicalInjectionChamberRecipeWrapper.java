package mekanism.client.jei.machine.advanced;

import mekanism.api.gas.Gas;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.common.MekanismFluids;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class ChemicalInjectionChamberRecipeWrapper extends AdvancedMachineRecipeWrapper
{
	public ChemicalInjectionChamberRecipeWrapper(AdvancedMachineRecipe r)
	{
		super(r);
	}
	
	@Override
	public List<ItemStack> getFuelStacks(Gas gasType)
	{
		if(gasType == MekanismFluids.SulfuricAcid)
		{
			List<ItemStack> fuels = new ArrayList<>();
			fuels.addAll(OreDictionary.getOres("dustSulfur"));
			fuels.add(MekanismUtils.getFullGasTank(GasTankTier.BASIC, MekanismFluids.SulfuricAcid));
			
			return fuels;
		}
		else if(gasType == MekanismFluids.Water)
		{
			return ListUtils.asList(MekanismUtils.getFullGasTank(GasTankTier.BASIC, MekanismFluids.Water));
		}
		else if(gasType == MekanismFluids.HydrogenChloride)
		{
			List<ItemStack> fuels = new ArrayList<>();
			fuels.addAll(OreDictionary.getOres("dustSalt"));
			fuels.add(MekanismUtils.getFullGasTank(GasTankTier.BASIC, MekanismFluids.HydrogenChloride));
			
			return fuels;
		}

		return new ArrayList<>();
	}
}
