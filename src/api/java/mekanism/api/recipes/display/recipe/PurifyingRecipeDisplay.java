package mekanism.api.recipes.display.recipe;

import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a purification recipe display where two inputs are combined in a specific order to create a result, with the secondary input potentially being consumed each
/// tick.
///
/// @since 10.8.0
public record PurifyingRecipeDisplay(SlotDisplay ingredient, SlotDisplay secondIngredient, boolean perTickSecondary, SlotDisplay result, SlotDisplay craftingStation)
      implements TwoInputPerTickRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<PurifyingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_PURIFYING);

    public PurifyingRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(secondIngredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public Type<PurifyingRecipeDisplay> type() {
        return TYPE.value();
    }
}