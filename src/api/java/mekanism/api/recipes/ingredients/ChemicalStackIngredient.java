package mekanism.api.recipes.ingredients;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;

/**
 * Base implementation for how Mekanism handle's ChemicalStack Ingredients.
 */
public interface ChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends InputIngredient<@NonNull STACK> {

    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param chemical Input argument.
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    boolean testType(@Nonnull CHEMICAL chemical);

    /**
     * Base implementation for how Mekanism handle's GasStack Ingredients.
     *
     * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#gas()}.
     */
    interface GasStackIngredient extends ChemicalStackIngredient<Gas, GasStack> {
    }

    /**
     * Base implementation for how Mekanism handle's InfusionStack Ingredients.
     *
     * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#infusion()}.
     */
    interface InfusionStackIngredient extends ChemicalStackIngredient<InfuseType, InfusionStack> {
    }

    /**
     * Base implementation for how Mekanism handle's PigmentStack Ingredients.
     *
     * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#pigment()}.
     */
    interface PigmentStackIngredient extends ChemicalStackIngredient<Pigment, PigmentStack> {
    }

    /**
     * Base implementation for how Mekanism handle's SlurryStack Ingredients.
     *
     * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#slurry()}.
     */
    interface SlurryStackIngredient extends ChemicalStackIngredient<Slurry, SlurryStack> {
    }
}