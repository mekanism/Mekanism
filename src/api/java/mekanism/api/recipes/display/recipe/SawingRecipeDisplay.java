package mekanism.api.recipes.display.recipe;

import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.display.slot.ChanceSlotDisplay;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents an activating recipe display where a singular input produces a result.
///
/// @implNote It is assumed that a [composite display][SlotDisplay.Composite] that has:
/// - the first element as a [chance display][ChanceSlotDisplay] is a grouping of multiple [chance displays][ChanceSlotDisplay].
/// - a size of `2`, represents the main output with the first display, and the chance output with the second display.
/// @since 10.8.0
public record SawingRecipeDisplay(SlotDisplay ingredient, SlotDisplay result, SlotDisplay craftingStation) implements OneInputRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<SawingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_SAWING);

    public SawingRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public Type<SawingRecipeDisplay> type() {
        return TYPE.value();
    }

    @Override
    public List<SlotDisplay> rawOutputs() {
        DecomposedDisplay decomposed = decomposeDisplay(result);
        return List.of(decomposed.mainOutput(), decomposed.secondaryOutput());
    }

    /// Decomposes a result display into one that has a main output and a secondary output as separate displays.
    public static DecomposedDisplay decomposeDisplay(SlotDisplay result) {
        //TODO: Re-evaluate if this is how we actually want to be doing this
        SlotDisplay mainOutputDisplay = SlotDisplay.Empty.INSTANCE;
        SlotDisplay secondaryOutputDisplay = SlotDisplay.Empty.INSTANCE;
        if (result instanceof ChanceSlotDisplay) {
            secondaryOutputDisplay = result;
        } else if (result instanceof SlotDisplay.Composite(List<SlotDisplay> contents) && !contents.isEmpty()) {
            if (contents.getFirst() instanceof ChanceSlotDisplay) {
                //Assume it is a list of chance displays
                secondaryOutputDisplay = result;
            } else if (contents.size() == 2) {
                mainOutputDisplay = contents.getFirst();
                secondaryOutputDisplay = contents.getLast();
            }
        }
        if (mainOutputDisplay == SlotDisplay.Empty.INSTANCE && secondaryOutputDisplay == SlotDisplay.Empty.INSTANCE) {
            mainOutputDisplay = result;
        }
        return new DecomposedDisplay(mainOutputDisplay, secondaryOutputDisplay);
    }

    public record DecomposedDisplay(SlotDisplay mainOutput, SlotDisplay secondaryOutput) {
    }
}