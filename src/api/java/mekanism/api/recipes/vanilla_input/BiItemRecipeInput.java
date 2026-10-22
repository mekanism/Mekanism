package mekanism.api.recipes.vanilla_input;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/// Simple implementation of a recipe input of two items.
///
/// @since 10.8.0
public record BiItemRecipeInput(ItemStack main, ItemStack secondary) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        if (index == 0) {
            return main;
        } else if (index == 1) {
            return secondary;
        }
        throw new IllegalArgumentException("No item for index " + index);
    }

    @Override
    public int size() {
        return 2;
    }
}