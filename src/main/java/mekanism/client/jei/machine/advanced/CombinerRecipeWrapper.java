package mekanism.client.jei.machine.advanced;

import mekanism.api.gas.Gas;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.util.ListUtils;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.List;

public class CombinerRecipeWrapper extends AdvancedMachineRecipeWrapper
{
	public CombinerRecipeWrapper(AdvancedMachineRecipe r)
	{
		super(r);
	}
	
	@Override
	public List<ItemStack> getFuelStacks(Gas gasType)
	{
		return ListUtils.asList(new ItemStack(Blocks.COBBLESTONE));
	}
}
