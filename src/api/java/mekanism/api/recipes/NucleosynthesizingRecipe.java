package mekanism.api.recipes;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;

/**
 * Extension of {@link ItemStackGasToItemStackRecipe} with a defined amount of ticks needed to process. Input: ItemStack
 * <br>
 * Input: Gas (Base value, will be multiplied by a per tick amount)
 * <br>
 * Output: ItemStack
 *
 * @apiNote Nucleosynthesizers can process this recipe type.
 */
@NothingNullByDefault
public abstract class NucleosynthesizingRecipe extends ItemStackGasToItemStackRecipe {

    private final int duration;

    /**
     * @param itemInput Item input.
     * @param gasInput  Gas input.
     * @param output    Output.
     * @param duration  Duration in ticks that it takes the recipe to complete. Must be greater than zero.
     */
    public NucleosynthesizingRecipe(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output, int duration) {
        super(itemInput, gasInput, output);
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be a number greater than zero.");
        }
        this.duration = duration;
    }

    /**
     * Gets the duration in ticks this recipe takes to complete.
     */
    public int getDuration() {
        return duration;
    }
}