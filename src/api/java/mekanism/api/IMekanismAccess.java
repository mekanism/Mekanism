package mekanism.api;

import java.util.ServiceLoader;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.integration.emi.IMekanismEmiHelper;
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
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
    IMekanismAccess INSTANCE = ServiceLoader.load(IMekanismAccess.class).findFirst().orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IMekanismAccess found"));

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
     * Gets the gas stack ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#gasStack()} instead.
     */
    IChemicalStackIngredientCreator<Gas, GasStack, IGasIngredient, GasStackIngredient> gasStackIngredientCreator();

    /**
     * Gets the gas ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#gas()} instead.
     * @since 10.6.0
     */
    IChemicalIngredientCreator<Gas, IGasIngredient> gasIngredientCreator();

    /**
     * Gets the infusion stack ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#infusionStack()} instead.
     */
    IChemicalStackIngredientCreator<InfuseType, InfusionStack, IInfusionIngredient, InfusionStackIngredient> infusionStackIngredientCreator();

    /**
     * Gets the infusion ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#infusion()} instead.
     * @since 10.6.0
     */
    IChemicalIngredientCreator<InfuseType, IInfusionIngredient> infusionIngredientCreator();

    /**
     * Gets the pigment stack ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#pigmentStack()} instead.
     */
    IChemicalStackIngredientCreator<Pigment, PigmentStack, IPigmentIngredient, PigmentStackIngredient> pigmentStackIngredientCreator();

    /**
     * Gets the pigment ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#pigment()} instead.
     * @since 10.6.0
     */
    IChemicalIngredientCreator<Pigment, IPigmentIngredient> pigmentIngredientCreator();

    /**
     * Gets the slurry stack ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#slurryStack()} instead.
     */
    IChemicalStackIngredientCreator<Slurry, SlurryStack, ISlurryIngredient, SlurryStackIngredient> slurryStackIngredientCreator();

    /**
     * Gets the slurry ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#slurry()} instead.
     * @since 10.6.0
     */
    IChemicalIngredientCreator<Slurry, ISlurryIngredient> slurryIngredientCreator();
}