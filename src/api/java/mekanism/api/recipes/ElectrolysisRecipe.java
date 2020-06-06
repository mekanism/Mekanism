package mekanism.api.recipes;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ElectrolysisRecipe extends MekanismRecipe implements Predicate<@NonNull FluidStack> {

    private final FluidStackIngredient input;
    private final GasStack leftGasOutput;
    private final GasStack rightGasOutput;
    private final FloatingLong energyMultiplier;

    public ElectrolysisRecipe(ResourceLocation id, FluidStackIngredient input, FloatingLong energyMultiplier, GasStack leftGasOutput, GasStack rightGasOutput) {
        super(id);
        this.input = input;
        this.energyMultiplier = energyMultiplier;
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
    public boolean test(@Nonnull FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    @Contract(value = "_ -> new", pure = true)
    public Pair<@NonNull GasStack, @NonNull GasStack> getOutput(FluidStack input) {
        return Pair.of(leftGasOutput.copy(), rightGasOutput.copy());
    }

    public FloatingLong getEnergyMultiplier() {
        return energyMultiplier;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        energyMultiplier.writeToBuffer(buffer);
        leftGasOutput.writeToPacket(buffer);
        rightGasOutput.writeToPacket(buffer);
    }
}