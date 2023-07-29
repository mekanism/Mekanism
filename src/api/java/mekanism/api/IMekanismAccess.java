package mekanism.api;

import com.mojang.serialization.Codec;
import java.util.ServiceLoader;
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
import mekanism.api.robit.RobitSkin;

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
     * Codec for (de)serializing robit skins inline.
     *
     * @apiNote Use {@link mekanism.api.robit.RobitSkinSerializationHelper#DIRECT_CODEC} instead.
     */
    Codec<RobitSkin> robitSkinCodec();

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
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#gas()} instead.
     */
    IChemicalStackIngredientCreator<Gas, GasStack, GasStackIngredient> gasStackIngredientCreator();

    /**
     * Gets the infusion stack ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#infusion()} instead.
     */
    IChemicalStackIngredientCreator<InfuseType, InfusionStack, InfusionStackIngredient> infusionStackIngredientCreator();

    /**
     * Gets the pigment stack ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#pigment()} instead.
     */
    IChemicalStackIngredientCreator<Pigment, PigmentStack, PigmentStackIngredient> pigmentStackIngredientCreator();

    /**
     * Gets the slurry stack ingredient creator.
     *
     * @apiNote Use {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#slurry()} instead.
     */
    IChemicalStackIngredientCreator<Slurry, SlurryStack, SlurryStackIngredient> slurryStackIngredientCreator();
}