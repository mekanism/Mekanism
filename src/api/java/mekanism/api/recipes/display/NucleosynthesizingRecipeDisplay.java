package mekanism.api.recipes.display;

import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a nucleosynthesizing recipe display.
///
/// @since 10.8.0
public record NucleosynthesizingRecipeDisplay(SlotDisplay input, SlotDisplay secondaryInput, boolean perTickUsage, int duration, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<NucleosynthesizingRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "nucleosynthesizing"));

    @Override
    public Type<NucleosynthesizingRecipeDisplay> type() {
        return TYPE.value();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.input.isEnabled(enabledFeatures) && this.secondaryInput.isEnabled(enabledFeatures) && RecipeDisplay.super.isEnabled(enabledFeatures);
    }
}