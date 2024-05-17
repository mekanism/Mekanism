package mekanism.common.registries;

import mekanism.api.MekanismAPI;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredients.slurry.CompoundSlurryIngredient;
import mekanism.common.recipe.ingredients.slurry.DifferenceSlurryIngredient;
import mekanism.common.recipe.ingredients.slurry.EmptySlurryIngredient;
import mekanism.common.recipe.ingredients.slurry.IntersectionSlurryIngredient;
import mekanism.common.recipe.ingredients.slurry.SingleSlurryIngredient;
import mekanism.common.recipe.ingredients.slurry.TagSlurryIngredient;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.registration.DeferredMapCodecRegister;

public class MekanismSlurryIngredientTypes {

    private MekanismSlurryIngredientTypes() {
    }

    public static final DeferredMapCodecRegister<ISlurryIngredient> INGREDIENT_TYPES = new DeferredMapCodecRegister<>(MekanismAPI.SLURRY_INGREDIENT_TYPE_REGISTRY_NAME, Mekanism.MODID);

    public static final DeferredMapCodecHolder<ISlurryIngredient, CompoundSlurryIngredient> COMPOUND = INGREDIENT_TYPES.registerCodec("compound", () -> CompoundSlurryIngredient.CODEC);
    public static final DeferredMapCodecHolder<ISlurryIngredient, DifferenceSlurryIngredient> DIFFERENCE = INGREDIENT_TYPES.registerCodec("difference", () -> DifferenceSlurryIngredient.CODEC);
    public static final DeferredMapCodecHolder<ISlurryIngredient, EmptySlurryIngredient> EMPTY = INGREDIENT_TYPES.registerCodec("empty", () -> EmptySlurryIngredient.CODEC);
    public static final DeferredMapCodecHolder<ISlurryIngredient, IntersectionSlurryIngredient> INTERSECTION = INGREDIENT_TYPES.registerCodec("intersection", () -> IntersectionSlurryIngredient.CODEC);
    public static final DeferredMapCodecHolder<ISlurryIngredient, SingleSlurryIngredient> SINGLE = INGREDIENT_TYPES.registerCodec("single", () -> SingleSlurryIngredient.CODEC);
    public static final DeferredMapCodecHolder<ISlurryIngredient, TagSlurryIngredient> TAG = INGREDIENT_TYPES.registerCodec("tag", () -> TagSlurryIngredient.CODEC);
}