package mekanism.api.recipes.display.recipe;

import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a rotary recipe display where a chemical input is converted to a fluid output.
///
/// @since 10.8.0
public record CondensentratingRecipeDisplay(SlotDisplay ingredient, SlotDisplay result, SlotDisplay craftingStation) implements RotaryRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<CondensentratingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_ROTARY.withSuffix("_condensentrating"));

    public CondensentratingRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public Type<CondensentratingRecipeDisplay> type() {
        return TYPE.value();
    }
}