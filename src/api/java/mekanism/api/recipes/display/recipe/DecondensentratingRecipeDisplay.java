package mekanism.api.recipes.display.recipe;

import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a rotary recipe display where a fluid input is converted to a chemical output.
///
/// @since 10.8.0
public record DecondensentratingRecipeDisplay(SlotDisplay ingredient, SlotDisplay result, SlotDisplay craftingStation) implements RotaryRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<DecondensentratingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_ROTARY.withSuffix("_decondensentrating"));

    public DecondensentratingRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public Type<DecondensentratingRecipeDisplay> type() {
        return TYPE.value();
    }
}