package mekanism.api.recipes;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/**
 * Base class for defining ItemStack to fluid recipes.
 * <br>
 * Input: ItemStack
 * <br>
 * Output: FluidStack
 *
 * @apiNote There is currently no types of ItemStack to FluidStack recipe type
 */
@NothingNullByDefault
public abstract class ItemStackToFluidRecipe extends ItemInputRecipe<FluidStackTemplate> {
}
