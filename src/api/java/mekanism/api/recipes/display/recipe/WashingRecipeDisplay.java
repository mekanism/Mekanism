package mekanism.api.recipes.display.recipe;

import com.google.common.base.Preconditions;
import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a washing recipe display where two inputs are combined in a specific order to create a result.
///
/// @since 10.8.0
public record WashingRecipeDisplay(SlotDisplay ingredient, SlotDisplay secondIngredient, SlotDisplay result, SlotDisplay craftingStation, float experience)
      implements TwoInputRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<WashingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_WASHING);

    public WashingRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(secondIngredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
        Preconditions.checkArgument(experience >= 0, "Experience produced must not be negative");
    }

    @Override
    public Type<WashingRecipeDisplay> type() {
        return TYPE.value();
    }
}