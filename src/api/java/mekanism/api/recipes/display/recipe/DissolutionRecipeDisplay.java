package mekanism.api.recipes.display.recipe;

import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a chemical dissolution recipe display where two inputs are combined in a specific order to create a result.
///
/// @since 10.8.0
public record DissolutionRecipeDisplay(SlotDisplay ingredient, SlotDisplay secondIngredient, SlotDisplay result, SlotDisplay craftingStation) implements TwoInputRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<DissolutionRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_DISSOLUTION);

    public DissolutionRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(secondIngredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public Type<DissolutionRecipeDisplay> type() {
        return TYPE.value();
    }
}