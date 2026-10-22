package mekanism.api.recipes.display.recipe;

import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents an enriching recipe display where a singular input produces a result.
///
/// @since 10.8.0
public record EnrichingRecipeDisplay(SlotDisplay ingredient, SlotDisplay result, SlotDisplay craftingStation) implements OneInputRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<EnrichingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_ENRICHING);

    public EnrichingRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public Type<EnrichingRecipeDisplay> type() {
        return TYPE.value();
    }
}