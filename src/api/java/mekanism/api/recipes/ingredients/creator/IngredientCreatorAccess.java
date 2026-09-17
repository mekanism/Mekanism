package mekanism.api.recipes.ingredients.creator;

import mekanism.api.IMekanismAccess;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.jspecify.annotations.Nullable;

/// Provides access to helpers for creating various types of ingredients.
public class IngredientCreatorAccess {

    private IngredientCreatorAccess() {
    }

    /// Gets the item stack ingredient creator.
    public static IItemStackIngredientCreator item() {
        return IMekanismAccess.INSTANCE.itemStackIngredientCreator();
    }

    /// Gets the fluid stack ingredient creator.
    public static IFluidStackIngredientCreator fluid() {
        return IMekanismAccess.INSTANCE.fluidStackIngredientCreator();
    }

    /// Gets the chemical stack ingredient creator.
    ///
    /// @see #chemical()
    /// @since 10.7.0
    public static IChemicalStackIngredientCreator chemicalStack() {
        return IMekanismAccess.INSTANCE.chemicalStackIngredientCreator();
    }

    /// Gets the chemical ingredient creator.
    ///
    /// @see #chemicalStack()
    /// @since 10.7.0
    public static IChemicalIngredientCreator chemical() {
        return IMekanismAccess.INSTANCE.chemicalIngredientCreator();
    }

    /// Private helper to create a predicate out of a component patch.
    ///
    /// @since 10.6.0
    @Nullable
    static DataComponentExactPredicate getComponentPatchPredicate(DataComponentPatch patch) {
        if (!patch.isEmpty()) {
            DataComponentExactPredicate.Builder builder = DataComponentExactPredicate.builder();
            for (DataComponentType<?> type : patch.keySet()) {
                expectComponent(patch, type, builder);
            }
            return builder.build();
        }
        return null;
    }

    private static <TYPE> void expectComponent(DataComponentPatch patch, DataComponentType<TYPE> type, DataComponentExactPredicate.Builder builder) {
        TYPE value = patch.getPatch(type);
        //Note: We only add if the value is added, we don't check ones that have been removed from default, as that isn't easily feasible
        if (value != null) {
            builder.expect(type, value);
        }
    }
}