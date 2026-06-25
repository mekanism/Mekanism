package mekanism.common.registries;

import mekanism.api.MekanismRegistries;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.CustomDisplayChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IntersectionChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.SimpleChemicalIngredient;
import mekanism.common.Mekanism;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.registration.DeferredMapCodecRegister;

public class MekanismChemicalIngredientTypes {

    private MekanismChemicalIngredientTypes() {
    }

    public static final DeferredMapCodecRegister<ChemicalIngredient> INGREDIENT_TYPES = new DeferredMapCodecRegister<>(MekanismRegistries.Keys.CHEMICAL_INGREDIENT_TYPES, Mekanism.MODID);

    public static final DeferredMapCodecHolder<ChemicalIngredient, SimpleChemicalIngredient> SIMPLE = INGREDIENT_TYPES.registerCodec("single", () -> SimpleChemicalIngredient.CODEC);
    public static final DeferredMapCodecHolder<ChemicalIngredient, CompoundChemicalIngredient> COMPOUND = INGREDIENT_TYPES.registerCodec("compound", () -> CompoundChemicalIngredient.CODEC);
    public static final DeferredMapCodecHolder<ChemicalIngredient, DifferenceChemicalIngredient> DIFFERENCE = INGREDIENT_TYPES.registerCodec("difference", () -> DifferenceChemicalIngredient.CODEC);
    public static final DeferredMapCodecHolder<ChemicalIngredient, IntersectionChemicalIngredient> INTERSECTION = INGREDIENT_TYPES.registerCodec("intersection", () -> IntersectionChemicalIngredient.CODEC);
    public static final DeferredMapCodecHolder<ChemicalIngredient, CustomDisplayChemicalIngredient> CUSTOM_DISPLAY = INGREDIENT_TYPES.registerCodec("custom_display", () -> CustomDisplayChemicalIngredient.CODEC);
}