package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public abstract class BasicItemStackToFluidRecipe extends ItemStackToFluidRecipe {

    protected final ItemStackIngredient input;
    protected final FluidStack output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicItemStackToFluidRecipe(ItemStackIngredient input, FluidStack output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return input.test(itemStack);
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public FluidStack getOutput(ItemStack input) {
        return output.copy();
    }

    @Override
    public List<FluidStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToFluidRecipe other = (BasicItemStackToFluidRecipe) o;
        return input.equals(other.input) && FluidStack.matches(output, other.output);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + FluidStack.hashFluidAndComponents(output);
        hash = 31 * hash + output.getAmount();
        return hash;
    }
}