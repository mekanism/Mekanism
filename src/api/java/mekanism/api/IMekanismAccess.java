package mekanism.api;

import mekanism.api.integration.emi.IMekanismEmiHelper;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mekanism.api.recipes.ingredients.creator.IChemicalIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;

/**
 * Provides access to a variety of different helpers that are exposed to the API.
 *
 * @since 10.4.0
 */
public interface IMekanismAccess {

    /**
     * Provides access to Mekanism's internals.
     */
    IMekanismAccess INSTANCE = MekanismAPI.getService(IMekanismAccess.class);

    /**
     * Gets a helper to interact with some of Mekanism's JEI integration internals. This should only be called if JEI is loaded.
     *
     * @throws IllegalStateException if JEI is not loaded.
     */
    IMekanismJEIHelper jeiHelper();

    /**
     * Gets a helper to interact with some of Mekanism's EMI integration internals. This should only be called if EMI is loaded.
     *
     * @throws IllegalStateException if EMI is not loaded.
     * @since 10.5.10
     */
    IMekanismEmiHelper emiHelper();

    /**
     * Gets the item stack ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#item()} instead.
     */
    IItemStackIngredientCreator itemStackIngredientCreator();

    /**
     * Gets the fluid stack ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#fluid()} instead.
     */
    IFluidStackIngredientCreator fluidStackIngredientCreator();

    /**
     * Gets the chemical stack ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#chemicalStack()} instead.
     * @since 10.7.0
     */
    IChemicalStackIngredientCreator chemicalStackIngredientCreator();

    /**
     * Gets the chemical ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#chemical()} instead.
     * @since 10.7.0
     */
    IChemicalIngredientCreator chemicalIngredientCreator();
}