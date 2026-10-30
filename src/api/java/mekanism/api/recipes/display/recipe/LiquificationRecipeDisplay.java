package mekanism.api.recipes.display.recipe;

import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.WithRemainder;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a nutritional liquification recipe display where a singular input produces a result.
///
/// @since 10.8.0
public record LiquificationRecipeDisplay(SlotDisplay ingredient, SlotDisplay result, SlotDisplay craftingStation) implements OneInputRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<LiquificationRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_LIQUIFICATION);

    public LiquificationRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public Type<LiquificationRecipeDisplay> type() {
        return TYPE.value();
    }

    @Override
    public List<SlotDisplay> rawOutputs() {
        if (result() instanceof WithRemainder(SlotDisplay input, SlotDisplay remainder)) {
            //Treat the result as a fluid and an item output as resolving WithRemainder doesn't work properly for displays of different types
            return List.of(input, remainder);
        }
        return OneInputRecipeDisplay.super.rawOutputs();
    }
}