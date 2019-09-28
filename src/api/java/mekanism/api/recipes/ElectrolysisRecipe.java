package mekanism.api.recipes;

import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by Thiakil on 15/07/2019.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class ElectrolysisRecipe implements IMekanismRecipe, Predicate<@NonNull FluidStack> {

    private final FluidStackIngredient input;
    private final GasStack leftGasOutput;
    private final GasStack rightGasOutput;
    private final double energyUsage;

    public ElectrolysisRecipe(FluidStackIngredient input, double energyUsage, GasStack leftGasOutput, GasStack rightGasOutput) {
        this.input = input;
        this.energyUsage = energyUsage;
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

    public Pair<@NonNull GasStack, @NonNull GasStack> getOutput(FluidStack input) {
        return Pair.of(leftGasOutput.copy(), rightGasOutput.copy());
    }

    public double getEnergyUsage() {
        return energyUsage;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        buffer.writeDouble(energyUsage);
        leftGasOutput.writeToPacket(buffer);
        rightGasOutput.writeToPacket(buffer);
    }
}
