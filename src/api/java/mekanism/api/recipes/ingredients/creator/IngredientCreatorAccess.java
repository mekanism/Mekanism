package mekanism.api.recipes.ingredients.creator;

import java.util.Map;
import java.util.Optional;
import mekanism.api.IMekanismAccess;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
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
     * Gets the creator type for a given chemical.
     *
     * @param chemicalType Type of chemical.
     *
     * @return Chemical Stack ingredient creator.
     */
    public static IChemicalStackIngredientCreator<?, ?, ?> getCreatorForType(ChemicalType chemicalType) {
        return switch (chemicalType) {
            case GAS -> gas();
            case INFUSION -> infusion();
            case PIGMENT -> pigment();
            case SLURRY -> slurry();
        };
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
     * Gets the gas stack ingredient creator.
     */
    public static IChemicalStackIngredientCreator<Gas, GasStack, GasStackIngredient> gas() {
        return IMekanismAccess.INSTANCE.gasStackIngredientCreator();
    }

    /**
     * Gets the infusion stack ingredient creator.
     */
    public static IChemicalStackIngredientCreator<InfuseType, InfusionStack, InfusionStackIngredient> infusion() {
        return IMekanismAccess.INSTANCE.infusionStackIngredientCreator();
    }

    /**
     * Gets the pigment stack ingredient creator.
     */
    public static IChemicalStackIngredientCreator<Pigment, PigmentStack, PigmentStackIngredient> pigment() {
        return IMekanismAccess.INSTANCE.pigmentStackIngredientCreator();
    }

    /**
     * Gets the slurry stack ingredient creator.
     */
    public static IChemicalStackIngredientCreator<Slurry, SlurryStack, SlurryStackIngredient> slurry() {
        return IMekanismAccess.INSTANCE.slurryStackIngredientCreator();
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
                if (value.isPresent()) {
                    //noinspection rawtypes,unchecked
                    builder.expect((DataComponentType) entry.getKey(), value);
                }
            }
            return builder.build();
        }
        return null;
    }
}