package mekanism.api.recipes.display.recipe;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents an electrolysis recipe display where a singular input produces a result. The first element of the result is the left output, the second will be for the
/// right output.
///
/// @since 10.8.0
public record ElectrolysisRecipeDisplay(SlotDisplay ingredient, SlotDisplay.Composite result, SlotDisplay craftingStation, int energyMultiplier)
      implements OneInputRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<ElectrolysisRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_SEPARATING);

    public ElectrolysisRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
        Preconditions.checkArgument(result.contents().size() == 2, "Electrolysis results must have exactly two outputs.");
        Preconditions.checkArgument(energyMultiplier >= 1, "Energy multiplier must be at least one.");
    }

    public ElectrolysisRecipeDisplay(SlotDisplay ingredient, SlotDisplay leftResult, SlotDisplay rightResult, SlotDisplay craftingStation, int energyMultiplier) {
        this(ingredient, new SlotDisplay.Composite(List.of(leftResult, rightResult)), craftingStation, energyMultiplier);
    }

    public SlotDisplay leftResult() {
        return result.contents().getFirst();
    }

    public SlotDisplay rightResult() {
        return result.contents().getLast();
    }

    @Override
    public List<SlotDisplay> rawOutputs() {
        return List.of(leftResult(), rightResult());
    }

    @Override
    public Type<ElectrolysisRecipeDisplay> type() {
        return TYPE.value();
    }
}