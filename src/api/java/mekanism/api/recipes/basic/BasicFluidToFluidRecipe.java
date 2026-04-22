package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicFluidToFluidRecipe extends FluidToFluidRecipe {

    protected final FluidStackIngredient input;
    protected final FluidStackTemplate output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicFluidToFluidRecipe(FluidStackIngredient input, FluidStackTemplate output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        this.output = output;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    @Override
    public FluidStackIngredient getInput() {
        return input;
    }

    @Override
    public List<FluidStack> getOutputDefinition() {
        return Collections.singletonList(output.create());
    }

    @Override
    @Contract(pure = true)
    public FluidStackTemplate getOutput(FluidStack input) {
        return output;
    }

    public FluidStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicFluidToFluidRecipe> getSerializer() {
        return MekanismRecipeSerializers.EVAPORATING.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicFluidToFluidRecipe other = (BasicFluidToFluidRecipe) o;
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + output.hashCode();
        return hash;
    }
}