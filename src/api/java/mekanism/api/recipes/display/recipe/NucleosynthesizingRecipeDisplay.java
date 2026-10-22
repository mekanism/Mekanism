package mekanism.api.recipes.display.recipe;

import com.google.common.base.Preconditions;
import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a nucleosynthesizing recipe display.
///
/// @since 10.8.0
public record NucleosynthesizingRecipeDisplay(SlotDisplay ingredient, SlotDisplay secondIngredient, boolean perTickSecondary, SlotDisplay result,
                                              SlotDisplay craftingStation, int duration) implements TwoInputPerTickRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<NucleosynthesizingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_NUCLEOSYNTHESIZING);

    public NucleosynthesizingRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(secondIngredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
        Preconditions.checkArgument(duration > 0, "Duration must be a number greater than zero.");
    }

    @Override
    public Type<NucleosynthesizingRecipeDisplay> type() {
        return TYPE.value();
    }
}