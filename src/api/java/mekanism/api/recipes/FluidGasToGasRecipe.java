package mekanism.api.recipes;

import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class FluidGasToGasRecipe implements IMekanismRecipe, BiPredicate<@NonNull FluidStack, @NonNull GasStack> {

    private final GasStackIngredient gasInput;
    private final FluidStackIngredient fluidInput;
    private final GasStack outputRepresentation;

    public FluidGasToGasRecipe(FluidStackIngredient fluidInput, GasStackIngredient gasInput, GasStack outputRepresentation) {
        this.fluidInput = fluidInput;
        this.gasInput = gasInput;
        this.outputRepresentation = outputRepresentation;
    }

    @Override
    public boolean test(@NonNull FluidStack fluidStack, @NonNull GasStack gasStack) {
        return fluidInput.test(fluidStack) && gasInput.test(gasStack);
    }

    public FluidStackIngredient getFluidInput() {
        return fluidInput;
    }

    public GasStackIngredient getGasInput() {
        return gasInput;
    }

    public GasStack getOutputRepresentation() {
        return outputRepresentation;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public GasStack getOutput(FluidStack fluidStack, GasStack input) {
        return outputRepresentation.copy();
    }
}
