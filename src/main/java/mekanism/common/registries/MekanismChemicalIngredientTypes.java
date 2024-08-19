package mekanism.common.registries;

import mekanism.api.MekanismAPI;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.EmptyChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IntersectionChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.TagChemicalIngredient;
import mekanism.common.Mekanism;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.registration.DeferredMapCodecRegister;

public class MekanismChemicalIngredientTypes {

    private MekanismChemicalIngredientTypes() {
    }

    public static final DeferredMapCodecRegister<ChemicalIngredient> INGREDIENT_TYPES = new DeferredMapCodecRegister<>(MekanismAPI.CHEMICAL_INGREDIENT_TYPE_REGISTRY_NAME, Mekanism.MODID);

    public static final DeferredMapCodecHolder<ChemicalIngredient, CompoundChemicalIngredient> COMPOUND = INGREDIENT_TYPES.registerCodec("compound", () -> CompoundChemicalIngredient.CODEC);
    public static final DeferredMapCodecHolder<ChemicalIngredient, DifferenceChemicalIngredient> DIFFERENCE = INGREDIENT_TYPES.registerCodec("difference", () -> DifferenceChemicalIngredient.CODEC);
    public static final DeferredMapCodecHolder<ChemicalIngredient, EmptyChemicalIngredient> EMPTY = INGREDIENT_TYPES.registerCodec("empty", () -> EmptyChemicalIngredient.CODEC);
    public static final DeferredMapCodecHolder<ChemicalIngredient, IntersectionChemicalIngredient> INTERSECTION = INGREDIENT_TYPES.registerCodec("intersection", () -> IntersectionChemicalIngredient.CODEC);
    public static final DeferredMapCodecHolder<ChemicalIngredient, SingleChemicalIngredient> SINGLE = INGREDIENT_TYPES.registerCodec("single", () -> SingleChemicalIngredient.CODEC);
    public static final DeferredMapCodecHolder<ChemicalIngredient, TagChemicalIngredient> TAG = INGREDIENT_TYPES.registerCodec("tag", () -> TagChemicalIngredient.CODEC);
}