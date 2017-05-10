package mekanism.client.jei.machine.advanced;

import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.api.util.ListUtils;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class CombinerRecipeWrapper extends AdvancedMachineRecipeWrapper
{
	public CombinerRecipeWrapper(AdvancedMachineRecipe r, AdvancedMachineRecipeCategory c)
	{
		super(r, c);
	}
	
	@Override
	public List<ItemStack> getFuelStacks(Gas gasType)
	{
		return ListUtils.asList(new ItemStack(Blocks.COBBLESTONE));
	}
}
