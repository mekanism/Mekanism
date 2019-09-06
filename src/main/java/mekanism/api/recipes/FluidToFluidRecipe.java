package mekanism.api.recipes;

import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidToFluidRecipe implements IMekanismRecipe, Predicate<@NonNull FluidStack> {

    private final FluidStackIngredient input;

    private final FluidStack outputRepresentation;

    public FluidToFluidRecipe(FluidStackIngredient input, FluidStack outputRepresentation) {
        this.input = input;
        this.outputRepresentation = outputRepresentation;
    }

    @Override
    public boolean test(@NonNull FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    public FluidStackIngredient getInput() {
        return input;
    }

    public FluidStack getOutputRepresentation() {
        return outputRepresentation;
    }

    @Contract(value = "_->new", pure = true)
    public FluidStack getOutput(FluidStack input) {
        return this.outputRepresentation.copy();
    }
}
