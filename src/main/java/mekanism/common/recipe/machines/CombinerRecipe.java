package mekanism.common.recipe.machines;

import net.minecraft.item.ItemStack;

public class CombinerRecipe extends AdvancedMachineRecipe
{
	public CombinerRecipe(ItemStack input, ItemStack output)
	{
		super(input, "liquidStone", output);
	}
}
