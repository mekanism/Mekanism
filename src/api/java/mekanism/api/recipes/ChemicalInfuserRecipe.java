package mekanism.api.recipes;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import org.jetbrains.annotations.Contract;

/**
 * Input: Two gases. The order of them does not matter.
 * <br>
 * Output: GasStack
 *
 * @apiNote Chemical Infusers can process this recipe type and the gases can be put in any order into the infuser.
 */
@NothingNullByDefault
public abstract class ChemicalInfuserRecipe extends ChemicalChemicalToChemicalRecipe<Gas, GasStack, GasStackIngredient> {

    @Override
    public abstract boolean test(GasStack input1, GasStack input2);

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public abstract GasStack getOutput(GasStack input1, GasStack input2);

    @Override
    public abstract GasStackIngredient getLeftInput();

    @Override
    public abstract GasStackIngredient getRightInput();

    @Override
    public abstract List<GasStack> getOutputDefinition();
}
