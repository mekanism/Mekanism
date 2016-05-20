package mekanism.client.jei.machine.advanced;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.util.ListUtils;
import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class PurificationChamberRecipeWrapper extends AdvancedMachineRecipeWrapper
{
	public PurificationChamberRecipeWrapper(AdvancedMachineRecipe r, AdvancedMachineRecipeCategory c)
	{
		super(r, c);
	}
	
	@Override
	public List<ItemStack> getFuelStacks(Gas gasType)
	{
		if(gasType == GasRegistry.getGas("oxygen"))
		{
			return ListUtils.asList(new ItemStack(Items.flint), MekanismUtils.getFullGasTank(GasTankTier.BASIC, GasRegistry.getGas("oxygen")));
		}

		return new ArrayList<ItemStack>();
	}
}
