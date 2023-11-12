package mekanism.api.recipes;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import org.jetbrains.annotations.Contract;

/**
 * Input: Gas
 * <br>
 * Output: GasStack
 *
 * @apiNote There are currently two types of Gas to Gas recipe types:
 * <ul>
 *     <li>Activating: Can be processed in a Solar Neutron Activator.</li>
 *     <li>Centrifuging: Can be processed in an Isotopic Centrifuge.</li>
 * </ul>
 */
@NothingNullByDefault
public abstract class GasToGasRecipe extends ChemicalToChemicalRecipe<Gas, GasStack, GasStackIngredient> {

    @Override
    public abstract boolean test(GasStack chemicalStack);

    @Override
    public abstract GasStackIngredient getInput();

    @Override
    public abstract List<GasStack> getOutputDefinition();

    @Override
    @Contract(value = "_ -> new", pure = true)
    public abstract GasStack getOutput(GasStack input);
}
