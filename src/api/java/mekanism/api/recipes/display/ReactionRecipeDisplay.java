package mekanism.api.recipes.display;

import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a reaction recipe display where three inputs produces a result.
///
/// @since 10.8.0
public record ReactionRecipeDisplay(SlotDisplay inputSolid, SlotDisplay inputFluid, SlotDisplay inputChemical, SlotDisplay result, SlotDisplay craftingStation) implements RecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<ReactionRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "reaction"));

    @Override
    public Type<ReactionRecipeDisplay> type() {
        return TYPE.value();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.inputSolid.isEnabled(enabledFeatures) && this.inputFluid.isEnabled(enabledFeatures) && this.inputChemical.isEnabled(enabledFeatures) &&
               RecipeDisplay.super.isEnabled(enabledFeatures);
    }
}