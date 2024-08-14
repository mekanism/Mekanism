package mekanism.api.recipes.vanilla_input;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.ItemStack;

/**
 * Simple implementation of a recipe input of one item and one chemical.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public record SingleItemChemicalRecipeInput(ItemStack item, ChemicalStack chemical) implements ItemChemicalRecipeInput {

    @Override
    public ItemStack getItem(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("No item for index " + index);
        }
        return item;
    }

    @Override
    public ChemicalStack getChemical(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("No chemical for index " + index);
        }
        return chemical;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty() || chemical.isEmpty();
    }
}