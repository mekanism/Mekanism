package mekanism.common.service;

import mekanism.api.IMekanismAccess;
import mekanism.api.MekanismAPI;
import mekanism.api.integration.emi.IMekanismEmiHelper;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredients.ChemicalIngredientCreator;
import mekanism.common.recipe.ingredients.creator.ChemicalStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.ItemStackIngredientCreator;
import org.jspecify.annotations.Nullable;

/// @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via [IMekanismAccess#INSTANCE]
public class MekanismAccess implements IMekanismAccess {

    @Nullable
    private IMekanismEmiHelper emiHelper;
    @Nullable
    private IMekanismJEIHelper jeiHelper;

    @Override
    public IMekanismJEIHelper jeiHelper() {
        Mekanism.hooks.jei.assertLoaded();
        if (jeiHelper == null) {
            //Lazily get the service, and don't throw if we fail as we want to be able to provide a better error message
            jeiHelper = MekanismAPI.getOptionalService(IMekanismJEIHelper.class);
            if (jeiHelper == null) {
                throw new UnsupportedOperationException("JEI Integration has not been updated");
            }
        }
        return jeiHelper;
    }

    @Override
    public IMekanismEmiHelper emiHelper() {
        Mekanism.hooks.emi.assertLoaded();
        if (emiHelper == null) {
            //Lazily get the service, and don't throw if we fail as we want to be able to provide a better error message
            emiHelper = MekanismAPI.getOptionalService(IMekanismEmiHelper.class);
            if (emiHelper == null) {
                throw new UnsupportedOperationException("EMI Integration has not been updated");
            }
        }
        return emiHelper;
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