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
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
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
    public static IChemicalStackIngredientCreator<?, ?, ?, ?> getCreatorForType(ChemicalType chemicalType) {
        return switch (chemicalType) {
            case GAS -> gasStack();
            case INFUSION -> infusionStack();
            case PIGMENT -> pigmentStack();
            case SLURRY -> slurryStack();
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
     *
     * @see #gas()
     * @since 10.6.0 Previously was named gas
     */
    public static IChemicalStackIngredientCreator<Gas, GasStack, IGasIngredient, GasStackIngredient> gasStack() {
        return IMekanismAccess.INSTANCE.gasStackIngredientCreator();
    }

    /**
     * Gets the gas ingredient creator.
     *
     * @see #gasStack()
     * @since 10.6.0
     */
    public static IChemicalIngredientCreator<Gas, IGasIngredient> gas() {
        return IMekanismAccess.INSTANCE.gasIngredientCreator();
    }

    /**
     * Gets the infusion stack ingredient creator.
     *
     * @see #infusion()
     * @since 10.6.0 Previously was named infusion
     */
    public static IChemicalStackIngredientCreator<InfuseType, InfusionStack, IInfusionIngredient, InfusionStackIngredient> infusionStack() {
        return IMekanismAccess.INSTANCE.infusionStackIngredientCreator();
    }

    /**
     * Gets the infusion ingredient creator.
     *
     * @see #infusionStack()
     * @since 10.6.0
     */
    public static IChemicalIngredientCreator<InfuseType, IInfusionIngredient> infusion() {
        return IMekanismAccess.INSTANCE.infusionIngredientCreator();
    }

    /**
     * Gets the pigment stack ingredient creator.
     *
     * @see #pigment()
     * @since 10.6.0 Previously was named pigment
     */
    public static IChemicalStackIngredientCreator<Pigment, PigmentStack, IPigmentIngredient, PigmentStackIngredient> pigmentStack() {
        return IMekanismAccess.INSTANCE.pigmentStackIngredientCreator();
    }

    /**
     * Gets the pigment ingredient creator.
     *
     * @see #pigmentStack()
     * @since 10.6.0
     */
    public static IChemicalIngredientCreator<Pigment, IPigmentIngredient> pigment() {
        return IMekanismAccess.INSTANCE.pigmentIngredientCreator();
    }

    /**
     * Gets the slurry stack ingredient creator.
     *
     * @see #slurry()
     * @since 10.6.0 Previously was named slurry
     */
    public static IChemicalStackIngredientCreator<Slurry, SlurryStack, ISlurryIngredient, SlurryStackIngredient> slurryStack() {
        return IMekanismAccess.INSTANCE.slurryStackIngredientCreator();
    }

    /**
     * Gets the slurry ingredient creator.
     *
     * @see #slurryStack()
     * @since 10.6.0
     */
    public static IChemicalIngredientCreator<Slurry, ISlurryIngredient> slurry() {
        return IMekanismAccess.INSTANCE.slurryIngredientCreator();
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