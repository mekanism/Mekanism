package mekanism.api.recipes;

import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import org.jetbrains.annotations.Contract;

/**
 * Created by Thiakil on 21/07/2019.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class GasToGasRecipe implements IMekanismRecipe, Predicate<@NonNull GasStack> {

    private final GasStackIngredient input;
    private final GasStack outputRepresentation;

    public GasToGasRecipe(GasStackIngredient input, GasStack outputRepresentation) {
        this.input = input;
        this.outputRepresentation = outputRepresentation;
    }

    @Override
    public boolean test(GasStack gasStack) {
        return input.test(gasStack);
    }

    public GasStackIngredient getInput() {
        return input;
    }

    public GasStack getOutputRepresentation() {
        return outputRepresentation;
    }

    @Contract(value = "_ -> new", pure = true)
    public GasStack getOutput(GasStack input) {
        return outputRepresentation.copy();
    }
}
