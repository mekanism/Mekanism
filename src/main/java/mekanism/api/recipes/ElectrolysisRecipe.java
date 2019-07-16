package mekanism.api.recipes;

import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by Thiakil on 15/07/2019.
 */
public class ElectrolysisRecipe implements Predicate<@NonNull FluidStack> {

    private final FluidStackIngredient input;

    private final GasStack leftGasOutput;

    private final GasStack rightGasOutput;

    public ElectrolysisRecipe(FluidStackIngredient input, GasStack leftGasOutput, GasStack rightGasOutput) {
        this.input = input;
        this.leftGasOutput = leftGasOutput;
        this.rightGasOutput = rightGasOutput;
    }

    public FluidStackIngredient getInput() {
        return input;
    }

    public GasStack getLeftGasOutputRepresentation() {
        return leftGasOutput;
    }

    public GasStack getRightGasOutputRepresentation() {
        return rightGasOutput;
    }

    @Override
    public boolean test(@NonNull FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    public Pair<GasStack, GasStack> getOutput(FluidStack input) {
        return Pair.of(leftGasOutput.copy(), rightGasOutput.copy());
    }

}
