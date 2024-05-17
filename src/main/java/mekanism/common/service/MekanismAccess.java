package mekanism.common.service;

import mekanism.api.IMekanismAccess;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.api.integration.emi.IMekanismEmiHelper;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.client.recipe_viewer.emi.MekanismEmiHelper;
import mekanism.client.recipe_viewer.jei.MekanismJEIHelper;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ingredients.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.GasStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.InfusionStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.ItemStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.PigmentStackIngredientCreator;
import mekanism.common.recipe.ingredients.creator.SlurryStackIngredientCreator;
import mekanism.common.recipe.ingredients.gas.GasIngredientCreator;
import mekanism.common.recipe.ingredients.infusion.InfusionIngredientCreator;
import mekanism.common.recipe.ingredients.pigment.PigmentIngredientCreator;
import mekanism.common.recipe.ingredients.slurry.SlurryIngredientCreator;

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
    public IChemicalStackIngredientCreator<Gas, GasStack, IGasIngredient, GasStackIngredient> gasStackIngredientCreator() {
        return GasStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalIngredientCreator<Gas, IGasIngredient> gasIngredientCreator() {
        return GasIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalStackIngredientCreator<InfuseType, InfusionStack, IInfusionIngredient, InfusionStackIngredient> infusionStackIngredientCreator() {
        return InfusionStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalIngredientCreator<InfuseType, IInfusionIngredient> infusionIngredientCreator() {
        return InfusionIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalStackIngredientCreator<Pigment, PigmentStack, IPigmentIngredient, PigmentStackIngredient> pigmentStackIngredientCreator() {
        return PigmentStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalIngredientCreator<Pigment, IPigmentIngredient> pigmentIngredientCreator() {
        return PigmentIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalStackIngredientCreator<Slurry, SlurryStack, ISlurryIngredient, SlurryStackIngredient> slurryStackIngredientCreator() {
        return SlurryStackIngredientCreator.INSTANCE;
    }

    @Override
    public IChemicalIngredientCreator<Slurry, ISlurryIngredient> slurryIngredientCreator() {
        return SlurryIngredientCreator.INSTANCE;
    }
}