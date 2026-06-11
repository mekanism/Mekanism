package mekanism.api.recipes;

import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/// Base class for defining ItemStack to fluid recipes.
///
/// Input: ItemStack
///
/// Output: FluidStack
///
/// @apiNote There is currently no types of ItemStack to FluidStack recipe type
public abstract class ItemStackToFluidRecipe extends ItemInputRecipe<FluidStackTemplate> {
}
