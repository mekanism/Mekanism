package mekanism.api.recipes.ingredients.creator;

import java.util.function.Consumer;
import mekanism.api.MekanismAPI;
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

/**
 * Provides access to helpers for creating various types of ingredients.
 */
public class IngredientCreatorAccess {

    private IngredientCreatorAccess() {
    }

    private static IItemStackIngredientCreator ITEM_STACK_INGREDIENT_CREATOR;
    private static IFluidStackIngredientCreator FLUID_STACK_INGREDIENT_CREATOR;
    private static IChemicalStackIngredientCreator<Gas, GasStack, GasStackIngredient> GAS_STACK_INGREDIENT_CREATOR;
    private static IChemicalStackIngredientCreator<InfuseType, InfusionStack, InfusionStackIngredient> INFUSION_STACK_INGREDIENT_CREATOR;
    private static IChemicalStackIngredientCreator<Pigment, PigmentStack, PigmentStackIngredient> PIGMENT_STACK_INGREDIENT_CREATOR;
    private static IChemicalStackIngredientCreator<Slurry, SlurryStack, SlurryStackIngredient> SLURRY_STACK_INGREDIENT_CREATOR;

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
        if (ITEM_STACK_INGREDIENT_CREATOR == null) {//Harmless race
            lookupInstance(IItemStackIngredientCreator.class, "mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator",
                  helper -> ITEM_STACK_INGREDIENT_CREATOR = helper);
        }
        return ITEM_STACK_INGREDIENT_CREATOR;
    }

    /**
     * Gets the fluid stack ingredient creator.
     */
    public static IFluidStackIngredientCreator fluid() {
        if (FLUID_STACK_INGREDIENT_CREATOR == null) {//Harmless race
            lookupInstance(IFluidStackIngredientCreator.class, "mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator",
                  helper -> FLUID_STACK_INGREDIENT_CREATOR = helper);
        }
        return FLUID_STACK_INGREDIENT_CREATOR;
    }

    /**
     * Gets the gas stack ingredient creator.
     */
    public static IChemicalStackIngredientCreator<Gas, GasStack, GasStackIngredient> gas() {
        if (GAS_STACK_INGREDIENT_CREATOR == null) {//Harmless race
            lookupInstance(IChemicalStackIngredientCreator.class, "mekanism.common.recipe.ingredient.creator.GasStackIngredientCreator",
                  helper -> GAS_STACK_INGREDIENT_CREATOR = helper);
        }
        return GAS_STACK_INGREDIENT_CREATOR;
    }

    /**
     * Gets the infusion stack ingredient creator.
     */
    public static IChemicalStackIngredientCreator<InfuseType, InfusionStack, InfusionStackIngredient> infusion() {
        if (INFUSION_STACK_INGREDIENT_CREATOR == null) {//Harmless race
            lookupInstance(IChemicalStackIngredientCreator.class, "mekanism.common.recipe.ingredient.creator.InfusionStackIngredientCreator",
                  helper -> INFUSION_STACK_INGREDIENT_CREATOR = helper);
        }
        return INFUSION_STACK_INGREDIENT_CREATOR;
    }

    /**
     * Gets the pigment stack ingredient creator.
     */
    public static IChemicalStackIngredientCreator<Pigment, PigmentStack, PigmentStackIngredient> pigment() {
        if (PIGMENT_STACK_INGREDIENT_CREATOR == null) {//Harmless race
            lookupInstance(IChemicalStackIngredientCreator.class, "mekanism.common.recipe.ingredient.creator.PigmentStackIngredientCreator",
                  helper -> PIGMENT_STACK_INGREDIENT_CREATOR = helper);
        }
        return PIGMENT_STACK_INGREDIENT_CREATOR;
    }

    /**
     * Gets the slurry stack ingredient creator.
     */
    public static IChemicalStackIngredientCreator<Slurry, SlurryStack, SlurryStackIngredient> slurry() {
        if (SLURRY_STACK_INGREDIENT_CREATOR == null) {//Harmless race
            lookupInstance(IChemicalStackIngredientCreator.class, "mekanism.common.recipe.ingredient.creator.SlurryStackIngredientCreator",
                  helper -> SLURRY_STACK_INGREDIENT_CREATOR = helper);
        }
        return SLURRY_STACK_INGREDIENT_CREATOR;
    }

    private static <TYPE extends IIngredientCreator<?, ?, ?>> void lookupInstance(Class<TYPE> type, String className, Consumer<TYPE> setter) {
        try {
            Class<?> clazz = Class.forName(className);
            setter.accept(type.cast(clazz.getField("INSTANCE").get(null)));
        } catch (ReflectiveOperationException ex) {
            MekanismAPI.logger.fatal("Error retrieving {}, Mekanism may be absent, damaged, or outdated.", className);
        }
    }
}