package mekanism.common.recipe.machines;

import net.minecraft.item.ItemStack;

public class PurificationRecipe extends AdvancedMachineRecipe
{
	public PurificationRecipe(ItemStack input, ItemStack output)
	{
		super(input, "oxygen", output);
	}
}
