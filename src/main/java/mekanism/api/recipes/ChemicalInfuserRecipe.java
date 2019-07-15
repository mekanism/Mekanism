package mekanism.api.recipes;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;

/**
 * Created by Thiakil on 13/07/2019.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalInfuserRecipe implements BiPredicate<@NonNull GasStack, @NonNull GasStack> {

    @NonNull
    private final GasStackIngredient leftInput;

    @NonNull
    private final GasStackIngredient rightInput;

    @NonNull
    private final Gas outputGas;

    private final int outputGasAmount;

    public ChemicalInfuserRecipe(GasStackIngredient leftInput, GasStackIngredient rightInput, Gas outputGas, int outputGasAmount) {
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.outputGas = outputGas;
        this.outputGasAmount = outputGasAmount;
    }

    @Override
    public boolean test(GasStack input1, GasStack input2) {
        return (leftInput.test(input1) && rightInput.test(input2)) || (rightInput.test(input1) && leftInput.test(input2));
    }

    public GasStack getOutput(GasStack input1, GasStack input2) {
        return new GasStack(outputGas, outputGasAmount);
    }

    public GasStackIngredient getLeftInput() {
        return leftInput;
    }

    public GasStackIngredient getRightInput() {
        return rightInput;
    }

    public Collection<GasStack> getOutputDefinition() {
        return Collections.singleton(new GasStack(outputGas, outputGasAmount));
    }
}
