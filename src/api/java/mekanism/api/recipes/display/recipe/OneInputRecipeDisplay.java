package mekanism.api.recipes.display.recipe;

import java.util.List;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/// Represents a recipe display where a singular input produces a result.
///
/// @since 10.8.0
public interface OneInputRecipeDisplay extends MekanismRecipeDisplay {

    /// {@return the display representing the input to this recipe}
    SlotDisplay ingredient();

    @Override
    default List<SlotDisplay> ingredients() {
        return List.of(ingredient());
    }
}