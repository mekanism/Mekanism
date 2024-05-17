package mekanism.common.registries;

import mekanism.api.MekanismAPI;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredients.pigment.CompoundPigmentIngredient;
import mekanism.common.recipe.ingredients.pigment.DifferencePigmentIngredient;
import mekanism.common.recipe.ingredients.pigment.EmptyPigmentIngredient;
import mekanism.common.recipe.ingredients.pigment.IntersectionPigmentIngredient;
import mekanism.common.recipe.ingredients.pigment.SinglePigmentIngredient;
import mekanism.common.recipe.ingredients.pigment.TagPigmentIngredient;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.registration.DeferredMapCodecRegister;

public class MekanismPigmentIngredientTypes {

    private MekanismPigmentIngredientTypes() {
    }

    public static final DeferredMapCodecRegister<IPigmentIngredient> INGREDIENT_TYPES = new DeferredMapCodecRegister<>(MekanismAPI.PIGMENT_INGREDIENT_TYPE_REGISTRY_NAME, Mekanism.MODID);

    public static final DeferredMapCodecHolder<IPigmentIngredient, CompoundPigmentIngredient> COMPOUND = INGREDIENT_TYPES.registerCodec("compound", () -> CompoundPigmentIngredient.CODEC);
    public static final DeferredMapCodecHolder<IPigmentIngredient, DifferencePigmentIngredient> DIFFERENCE = INGREDIENT_TYPES.registerCodec("difference", () -> DifferencePigmentIngredient.CODEC);
    public static final DeferredMapCodecHolder<IPigmentIngredient, EmptyPigmentIngredient> EMPTY = INGREDIENT_TYPES.registerCodec("empty", () -> EmptyPigmentIngredient.CODEC);
    public static final DeferredMapCodecHolder<IPigmentIngredient, IntersectionPigmentIngredient> INTERSECTION = INGREDIENT_TYPES.registerCodec("intersection", () -> IntersectionPigmentIngredient.CODEC);
    public static final DeferredMapCodecHolder<IPigmentIngredient, SinglePigmentIngredient> SINGLE = INGREDIENT_TYPES.registerCodec("single", () -> SinglePigmentIngredient.CODEC);
    public static final DeferredMapCodecHolder<IPigmentIngredient, TagPigmentIngredient> TAG = INGREDIENT_TYPES.registerCodec("tag", () -> TagPigmentIngredient.CODEC);
}