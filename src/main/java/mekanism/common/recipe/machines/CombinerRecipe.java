package mekanism.common.recipe.machines;

import mekanism.common.MekanismFluids;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import net.minecraft.item.ItemStack;

public class CombinerRecipe extends AdvancedMachineRecipe<CombinerRecipe>
{
	public CombinerRecipe(AdvancedMachineInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public CombinerRecipe(ItemStack input, ItemStack output)
	{
		super(input, MekanismFluids.LiquidStone, output);
	}

	@Override
	public CombinerRecipe copy()
	{
		return new CombinerRecipe(getInput().copy(), getOutput().copy());
	}
}
