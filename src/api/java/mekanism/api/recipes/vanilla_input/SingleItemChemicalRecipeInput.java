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

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SingleItemChemicalRecipeInput other = (SingleItemChemicalRecipeInput) o;
        return chemical.equals(other.chemical) && ItemStack.matches(item, other.item);
    }

    @Override
    public int hashCode() {
        int hash = chemical.hashCode();
        hash = 31 * hash + ItemStack.hashItemAndComponents(item);
        hash = 31 + hash + item.getCount();
        return hash;
    }
}