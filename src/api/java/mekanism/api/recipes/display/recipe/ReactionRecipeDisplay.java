package mekanism.api.recipes.display.recipe;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a reaction recipe display where three inputs produces a result.
///
/// @since 10.8.0
public record ReactionRecipeDisplay(SlotDisplay itemIngredient, SlotDisplay fluidIngredient, SlotDisplay chemicalIngredient, SlotDisplay result,
                                    SlotDisplay craftingStation, int duration, int energyRequired) implements MekanismRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<ReactionRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_REACTION);

    public ReactionRecipeDisplay {
        Objects.requireNonNull(itemIngredient);
        Objects.requireNonNull(fluidIngredient);
        Objects.requireNonNull(chemicalIngredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
        Preconditions.checkArgument(duration > 0, "Duration must be positive.");
        Preconditions.checkArgument(energyRequired >= 0, "Energy required must not be negative");
    }

    @Override
    public Type<ReactionRecipeDisplay> type() {
        return TYPE.value();
    }

    @Override
    public List<SlotDisplay> ingredients() {
        return List.of(itemIngredient, fluidIngredient, chemicalIngredient);
    }
}