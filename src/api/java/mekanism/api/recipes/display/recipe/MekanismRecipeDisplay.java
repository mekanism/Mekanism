package mekanism.api.recipes.display.recipe;

import java.util.List;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/// @since 10.8.0
public interface MekanismRecipeDisplay extends RecipeDisplay {

    /// {@return all input ingredient slot displays}
    List<SlotDisplay> ingredients();

    /// {@return the broken down output ingredient slot displays}
    default List<SlotDisplay> rawOutputs() {
        return List.of(result());
    }

    @Override
    default boolean isEnabled(FeatureFlagSet enabledFeatures) {
        for (SlotDisplay ingredient : ingredients()) {
            if (!ingredient.isEnabled(enabledFeatures)) {
                return false;
            }
        }
        return RecipeDisplay.super.isEnabled(enabledFeatures);
    }
}