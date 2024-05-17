package mekanism.common.registries;

import mekanism.api.MekanismAPI;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredients.infusion.CompoundInfusionIngredient;
import mekanism.common.recipe.ingredients.infusion.DifferenceInfusionIngredient;
import mekanism.common.recipe.ingredients.infusion.EmptyInfusionIngredient;
import mekanism.common.recipe.ingredients.infusion.IntersectionInfusionIngredient;
import mekanism.common.recipe.ingredients.infusion.SingleInfusionIngredient;
import mekanism.common.recipe.ingredients.infusion.TagInfusionIngredient;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.registration.DeferredMapCodecRegister;

public class MekanismInfusionIngredientTypes {

    private MekanismInfusionIngredientTypes() {
    }

    public static final DeferredMapCodecRegister<IInfusionIngredient> INGREDIENT_TYPES = new DeferredMapCodecRegister<>(MekanismAPI.INFUSION_INGREDIENT_TYPE_REGISTRY_NAME, Mekanism.MODID);

    public static final DeferredMapCodecHolder<IInfusionIngredient, CompoundInfusionIngredient> COMPOUND = INGREDIENT_TYPES.registerCodec("compound", () -> CompoundInfusionIngredient.CODEC);
    public static final DeferredMapCodecHolder<IInfusionIngredient, DifferenceInfusionIngredient> DIFFERENCE = INGREDIENT_TYPES.registerCodec("difference", () -> DifferenceInfusionIngredient.CODEC);
    public static final DeferredMapCodecHolder<IInfusionIngredient, EmptyInfusionIngredient> EMPTY = INGREDIENT_TYPES.registerCodec("empty", () -> EmptyInfusionIngredient.CODEC);
    public static final DeferredMapCodecHolder<IInfusionIngredient, IntersectionInfusionIngredient> INTERSECTION = INGREDIENT_TYPES.registerCodec("intersection", () -> IntersectionInfusionIngredient.CODEC);
    public static final DeferredMapCodecHolder<IInfusionIngredient, SingleInfusionIngredient> SINGLE = INGREDIENT_TYPES.registerCodec("single", () -> SingleInfusionIngredient.CODEC);
    public static final DeferredMapCodecHolder<IInfusionIngredient, TagInfusionIngredient> TAG = INGREDIENT_TYPES.registerCodec("tag", () -> TagInfusionIngredient.CODEC);
}