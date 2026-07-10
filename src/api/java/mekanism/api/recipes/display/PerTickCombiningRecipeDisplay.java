package mekanism.api.recipes.display;

import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a recipe display where two inputs are combined in a specific order to create a result, with the secondary input being consumed each tick.
///
/// @see CombiningRecipeDisplay For when the recipe always has a fixed secondary amount.
/// @since 10.8.0
public record PerTickCombiningRecipeDisplay(SlotDisplay input, SlotDisplay secondaryInput, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<PerTickCombiningRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "per_tick_combining"));

    @Override
    public Type<PerTickCombiningRecipeDisplay> type() {
        return TYPE.value();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.input.isEnabled(enabledFeatures) && this.secondaryInput.isEnabled(enabledFeatures) && RecipeDisplay.super.isEnabled(enabledFeatures);
    }

    /// Creates either a [PerTickCombiningRecipeDisplay] or a [CombiningRecipeDisplay] depending on whether the secondary input's usage is per tick.
    ///
    /// @param perTickUsage `true` to create a [PerTickCombiningRecipeDisplay], `false` to create a [CombiningRecipeDisplay]
    public static RecipeDisplay create(SlotDisplay input, SlotDisplay secondaryInput, boolean perTickUsage, SlotDisplay result, SlotDisplay craftingStation) {
        if (perTickUsage) {
            return new PerTickCombiningRecipeDisplay(input, secondaryInput, result, craftingStation);
        }
        return new CombiningRecipeDisplay(input, secondaryInput, result, craftingStation);
    }
}