package mekanism.api.recipes.display.recipe;

import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a combining recipe display where two inputs are combined in a specific order to create a result.
///
/// @since 10.8.0
public record CombiningRecipeDisplay(SlotDisplay ingredient, SlotDisplay secondIngredient, SlotDisplay result, SlotDisplay craftingStation) implements TwoInputRecipeDisplay {

    private static final DeferredHolder<RecipeDisplay.Type<?>, RecipeDisplay.Type<CombiningRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_COMBINING);

    public CombiningRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(secondIngredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public RecipeDisplay.Type<CombiningRecipeDisplay> type() {
        return TYPE.value();
    }
}