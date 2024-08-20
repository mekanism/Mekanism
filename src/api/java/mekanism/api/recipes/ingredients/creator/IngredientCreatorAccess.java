package mekanism.api.recipes.ingredients.creator;

import java.util.Map;
import java.util.Optional;
import mekanism.api.IMekanismAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

/**
 * Provides access to helpers for creating various types of ingredients.
 */
public class IngredientCreatorAccess {

    private IngredientCreatorAccess() {
    }

    /**
     * Gets the item stack ingredient creator.
     */
    public static IItemStackIngredientCreator item() {
        return IMekanismAccess.INSTANCE.itemStackIngredientCreator();
    }

    /**
     * Gets the fluid stack ingredient creator.
     */
    public static IFluidStackIngredientCreator fluid() {
        return IMekanismAccess.INSTANCE.fluidStackIngredientCreator();
    }

    /**
     * Gets the chemical stack ingredient creator.
     *
     * @see #chemical()
     * @since 10.7.0
     */
    public static IChemicalStackIngredientCreator chemicalStack() {
        return IMekanismAccess.INSTANCE.chemicalStackIngredientCreator();
    }

    /**
     * Gets the chemical ingredient creator.
     *
     * @see #chemicalStack()
     * @since 10.7.0
     */
    public static IChemicalIngredientCreator chemical() {
        return IMekanismAccess.INSTANCE.chemicalIngredientCreator();
    }

    /**
     * Private helper to create a predicate out of a component patch.
     *
     * @since 10.6.0
     */
    @Nullable
    static DataComponentPredicate getComponentPatchPredicate(DataComponentPatch patch) {
        if (!patch.isEmpty()) {
            DataComponentPredicate.Builder builder = DataComponentPredicate.builder();
            for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
                Optional<?> value = entry.getValue();
                //Note: We only add if the value is added, we don't check ones that have been removed from default, as that isn't easily feasible
                //noinspection OptionalIsPresent - Capturing lambda
                if (value.isPresent()) {
                    //noinspection rawtypes,unchecked
                    builder.expect((DataComponentType) entry.getKey(), value.get());
                }
            }
            return builder.build();
        }
        return null;
    }
}