package mekanism.api.recipes.vanilla_input;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Represents a recipe input that has an equal number of item and chemical inputs.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public interface ItemChemicalRecipeInput extends RecipeInput {

    ChemicalStack getChemical(int index);

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            if (!getItem(i).isEmpty() && !getChemical(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}