package mekanism.api.integration.jei;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mezz.jei.api.ingredients.IIngredientHelper;

/**
 * Helper for interacting with Mekanism's internals related to JEI. Get an instance via {@link mekanism.api.MekanismAPI#getJeiHelper()} after ensuring that JEI is
 * loaded.
 */
public interface IMekanismJEIHelper {

    /**
     * Gets the ingredient helper for {@link GasStack gases}.
     */
    IIngredientHelper<GasStack> getGasStackHelper();

    /**
     * Gets the ingredient helper for {@link InfusionStack infuse types}.
     */
    IIngredientHelper<InfusionStack> getInfusionStackHelper();

    /**
     * Gets the ingredient helper for {@link PigmentStack pigments}.
     */
    IIngredientHelper<PigmentStack> getPigmentStackHelper();

    /**
     * Gets the ingredient helper for {@link SlurryStack slurries}.
     */
    IIngredientHelper<SlurryStack> getSlurryStackHelper();
}