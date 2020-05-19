package mekanism.api.recipes.inputs.chemical;

import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.inputs.InputIngredient;

public interface IChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends InputIngredient<@NonNull STACK> {

    boolean testType(@Nonnull CHEMICAL chemical);

    /**
     * @apiNote This is for use in implementations and should probably not be accessed for other purposes
     */
    ChemicalIngredientInfo<CHEMICAL, STACK> getIngredientInfo();
}