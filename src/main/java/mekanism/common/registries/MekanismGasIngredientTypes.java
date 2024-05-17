package mekanism.common.registries;

import mekanism.api.MekanismAPI;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredients.gas.CompoundGasIngredient;
import mekanism.common.recipe.ingredients.gas.DifferenceGasIngredient;
import mekanism.common.recipe.ingredients.gas.EmptyGasIngredient;
import mekanism.common.recipe.ingredients.gas.IntersectionGasIngredient;
import mekanism.common.recipe.ingredients.gas.SingleGasIngredient;
import mekanism.common.recipe.ingredients.gas.TagGasIngredient;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.registration.DeferredMapCodecRegister;

public class MekanismGasIngredientTypes {

    private MekanismGasIngredientTypes() {
    }

    public static final DeferredMapCodecRegister<IGasIngredient> INGREDIENT_TYPES = new DeferredMapCodecRegister<>(MekanismAPI.GAS_INGREDIENT_TYPE_REGISTRY_NAME, Mekanism.MODID);

    public static final DeferredMapCodecHolder<IGasIngredient, CompoundGasIngredient> COMPOUND = INGREDIENT_TYPES.registerCodec("compound", () -> CompoundGasIngredient.CODEC);
    public static final DeferredMapCodecHolder<IGasIngredient, DifferenceGasIngredient> DIFFERENCE = INGREDIENT_TYPES.registerCodec("difference", () -> DifferenceGasIngredient.CODEC);
    public static final DeferredMapCodecHolder<IGasIngredient, EmptyGasIngredient> EMPTY = INGREDIENT_TYPES.registerCodec("empty", () -> EmptyGasIngredient.CODEC);
    public static final DeferredMapCodecHolder<IGasIngredient, IntersectionGasIngredient> INTERSECTION = INGREDIENT_TYPES.registerCodec("intersection", () -> IntersectionGasIngredient.CODEC);
    public static final DeferredMapCodecHolder<IGasIngredient, SingleGasIngredient> SINGLE = INGREDIENT_TYPES.registerCodec("single", () -> SingleGasIngredient.CODEC);
    public static final DeferredMapCodecHolder<IGasIngredient, TagGasIngredient> TAG = INGREDIENT_TYPES.registerCodec("tag", () -> TagGasIngredient.CODEC);
}