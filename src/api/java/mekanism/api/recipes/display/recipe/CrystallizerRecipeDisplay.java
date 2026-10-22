package mekanism.api.recipes.display.recipe;

import java.util.Objects;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Represents a crystallizer recipe display where a singular input produces a result, with an optional extra type info being displayed.
///
/// @since 10.8.0
public record CrystallizerRecipeDisplay(SlotDisplay ingredient, SlotDisplay typeDisplay, SlotDisplay result, SlotDisplay craftingStation) implements OneInputRecipeDisplay {

    private static final DeferredHolder<Type<?>, Type<CrystallizerRecipeDisplay>> TYPE = DeferredHolder.create(Registries.RECIPE_DISPLAY, MekanismRecipeTypes.NAME_CRYSTALLIZING);

    public CrystallizerRecipeDisplay {
        Objects.requireNonNull(ingredient);
        Objects.requireNonNull(typeDisplay);
        Objects.requireNonNull(result);
        Objects.requireNonNull(craftingStation);
    }

    @Override
    public Type<CrystallizerRecipeDisplay> type() {
        return TYPE.value();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.typeDisplay.isEnabled(enabledFeatures) && OneInputRecipeDisplay.super.isEnabled(enabledFeatures);
    }
}