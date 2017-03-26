package mekanism.client.jei.machine.advanced;

import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.util.ListUtils;
import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.common.MekanismItems;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

public class OsmiumCompressorRecipeWrapper extends AdvancedMachineRecipeWrapper
{
	public OsmiumCompressorRecipeWrapper(AdvancedMachineRecipe r, AdvancedMachineRecipeCategory c)
	{
		super(r, c);
	}
	
	@Override
	public List<ItemStack> getFuelStacks(Gas gasType)
	{
		return ListUtils.asList(new ItemStack(MekanismItems.Ingot, 1, 1));
	}
}
