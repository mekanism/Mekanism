package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class BasicFluidToFluidRecipe  extends FluidToFluidRecipe {

    protected final FluidStackIngredient input;
    protected final FluidStack output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicFluidToFluidRecipe(FluidStackIngredient input, FluidStack output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    @Override public FluidStackIngredient getInput() {
        return input;
    }

    @Override public List<FluidStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override@Contract(value = "_ ->new", pure = true)
    public FluidStack getOutput(FluidStack input) {
        return output.copy();
    }

    }