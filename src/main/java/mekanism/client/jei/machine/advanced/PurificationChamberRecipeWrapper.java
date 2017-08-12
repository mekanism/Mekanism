package mekanism.client.jei.machine.advanced;

import mekanism.api.gas.Gas;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.common.MekanismFluids;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PurificationChamberRecipeWrapper extends AdvancedMachineRecipeWrapper
{
	public PurificationChamberRecipeWrapper(AdvancedMachineRecipe r)
	{
		super(r);
	}
	
	@Override
	public List<ItemStack> getFuelStacks(Gas gasType)
	{
		if(gasType == MekanismFluids.Oxygen)
		{
			return ListUtils.asList(new ItemStack(Items.FLINT), MekanismUtils.getFullGasTank(GasTankTier.BASIC, MekanismFluids.Oxygen));
		}

		return new ArrayList<>();
	}
}
