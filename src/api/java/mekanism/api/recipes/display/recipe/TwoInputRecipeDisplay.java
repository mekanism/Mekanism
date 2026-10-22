package mekanism.api.recipes.display.recipe;

import java.util.List;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/// Represents a recipe display where two inputs are combined to create a result.
///
/// @see TwoInputPerTickRecipeDisplay For when the recipe may require the secondary input each tick.
/// @since 10.8.0
public interface TwoInputRecipeDisplay extends MekanismRecipeDisplay {

    /// {@return the display representing the first input to this recipe}
    SlotDisplay ingredient();

    /// {@return the display representing the second input to this recipe}
    SlotDisplay secondIngredient();

    @Override
    default List<SlotDisplay> ingredients() {
        return List.of(ingredient(), secondIngredient());
    }
}