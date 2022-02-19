package mekanism.api.recipes.inputs.chemical;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.inputs.InputIngredient;

/**
 * Base implementation for how Mekanism handle's ChemicalStack Ingredients.
 */
public interface IChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends InputIngredient<@NonNull STACK> {

    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param chemical Input argument.
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    boolean testType(@Nonnull CHEMICAL chemical);

    /**
     * @apiNote This is for use in implementations and should probably not be accessed for other purposes
     */
    ChemicalIngredientInfo<CHEMICAL, STACK> getIngredientInfo();
}