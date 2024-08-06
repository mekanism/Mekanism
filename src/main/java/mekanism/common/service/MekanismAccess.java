package mekanism.common.service;

import mekanism.api.IMekanismAccess;
import mekanism.api.integration.emi.IMekanismEmiHelper;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.client.recipe_viewer.emi.MekanismEmiHelper;
import mekanism.client.recipe_viewer.jei.MekanismJEIHelper;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredients.ChemicalIngredientCreator;
import mekanism.common.recipe.ingredients.creator.ChemicalStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.ItemStackIngredientCreator;

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
    public IMekanismEmiHelper emiHelper() {
        if (Mekanism.hooks.EmiLoaded) {
            return MekanismEmiHelper.INSTANCE;
        }
        throw new IllegalStateException("EMI is not loaded.");
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
    public IChemicalStackIngredientCreator chemicalStackIngredientCreator() {
        return ChemicalStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalIngredientCreator chemicalIngredientCreator() {
        return ChemicalIngredientCreator.INSTANCE;
    }
}