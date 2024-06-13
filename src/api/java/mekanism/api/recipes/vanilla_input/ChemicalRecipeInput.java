package mekanism.api.recipes.vanilla_input;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Represents a recipe input that for chemical inputs.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public interface ChemicalRecipeInput<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends RecipeInput {

    @Override
    default ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    /**
     * Gets the chemical with the given index.
     *
     * @param index Index to lookup.
     *
     * @return Chemical.
     */
    STACK getChemical(int index);

    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            if (!getChemical(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}