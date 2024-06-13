package mekanism.api.recipes.vanilla_input;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Simple implementation of a recipe input of one boxed chemical.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public record SingleBoxedChemicalInput(BoxedChemicalStack chemical) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return chemical.isEmpty();
    }
}