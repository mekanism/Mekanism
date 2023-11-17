package mekanism.common.service;

import mekanism.api.IMekanismAccess;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.client.jei.MekanismJEIHelper;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.GasStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.InfusionStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.PigmentStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.SlurryStackIngredientCreator;

/**
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via {@link IMekanismAccess#INSTANCE}
 */
public class MekanismAccess implements IMekanismAccess {

    @Override
    public IMekanismJEIHelper jeiHelper() {
        if (Mekanism.hooks.JEILoaded) {
            return MekanismJEIHelper.INSTANCE;
        }
        throw new IllegalStateException("JEI is not loaded.");
    }

    @Override
    public IItemStackIngredientCreator itemStackIngredientCreator() {
        return ItemStackIngredientCreator.INSTANCE;
    }

    @Override
    public IFluidStackIngredientCreator fluidStackIngredientCreator() {
        return FluidStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalStackIngredientCreator<Gas, GasStack, GasStackIngredient> gasStackIngredientCreator() {
        return GasStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalStackIngredientCreator<InfuseType, InfusionStack, InfusionStackIngredient> infusionStackIngredientCreator() {
        return InfusionStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalStackIngredientCreator<Pigment, PigmentStack, PigmentStackIngredient> pigmentStackIngredientCreator() {
        return PigmentStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalStackIngredientCreator<Slurry, SlurryStack, SlurryStackIngredient> slurryStackIngredientCreator() {
        return SlurryStackIngredientCreator.INSTANCE;
    }
}