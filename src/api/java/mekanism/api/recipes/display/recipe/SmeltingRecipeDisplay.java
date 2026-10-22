package mekanism.api.recipes.display.recipe;

import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a smelting recipe display where a singular input produces a result.
///
/// @since 10.8.0
public record SmeltingRecipeDisplay(SlotDisplay ingredient, SlotDisplay result, SlotDisplay craftingStation) implements OneInputRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<SmeltingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_SMELTING);

    public SmeltingRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public Type<SmeltingRecipeDisplay> type() {
        return TYPE.value();
    }
}