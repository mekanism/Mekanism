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
public class ElectrolysisRecipe implements IMekanismRecipe, Predicate<@NonNull FluidStack> {

    private final FluidStackIngredient input;
    @NonNull
    private final GasStack leftGasOutput;
    @NonNull
    private final GasStack rightGasOutput;
    private final double energyUsage;

    public ElectrolysisRecipe(FluidStackIngredient input, double energyUsage, @NonNull GasStack leftGasOutput, @NonNull  GasStack rightGasOutput) {
        this.input = input;
        this.energyUsage = energyUsage;
        this.leftGasOutput = leftGasOutput;
        this.rightGasOutput = rightGasOutput;
    }

    public FluidStackIngredient getInput() {
        return input;
    }

    @NonNull
    public GasStack getLeftGasOutputRepresentation() {
        return leftGasOutput;
    }

    @NonNull
    public GasStack getRightGasOutputRepresentation() {
        return rightGasOutput;
    }

    @Override
    public boolean test(@NonNull FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    public Pair<@NonNull GasStack, @NonNull GasStack> getOutput(FluidStack input) {
        return Pair.of(leftGasOutput.copy(), rightGasOutput.copy());
    }

    public double getEnergyUsage() {
        return energyUsage;
    }
}
