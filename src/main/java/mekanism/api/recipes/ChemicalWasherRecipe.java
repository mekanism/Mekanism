package mekanism.api.recipes;

import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class ChemicalWasherRecipe implements IMekanismRecipe, BiPredicate<@NonNull FluidStack, @NonNull GasStack> {

    private final GasStackIngredient input;
    private final FluidStackIngredient cleansingIngredient;

    private final GasStack outputRepresentation;

    public ChemicalWasherRecipe(FluidStackIngredient cleansingIngredient, GasStackIngredient input, GasStack outputRepresentation) {
        this.cleansingIngredient = cleansingIngredient;
        this.input = input;
        this.outputRepresentation = outputRepresentation;
    }

    @Override
    public boolean test(@NonNull FluidStack fluidStack, @NonNull GasStack gasStack) {
        return input.test(gasStack);
    }

    public FluidStackIngredient getCleansingIngredient() {
        return cleansingIngredient;
    }

    public GasStackIngredient getInput() {
        return input;
    }

    public GasStack getOutputRepresentation() {
        return outputRepresentation;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public GasStack getOutput(FluidStack fluidStack, GasStack input) {
        //TODO: Return the value based on the difference in size of the fluidstack?
        return outputRepresentation.copy();
    }
}
