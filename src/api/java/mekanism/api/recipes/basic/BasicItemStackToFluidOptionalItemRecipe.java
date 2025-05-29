package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;

/**
 * Basic implementation of {@link ItemStackToFluidOptionalItemRecipe}
 * @since 10.6.3
 */
@NothingNullByDefault
public abstract class BasicItemStackToFluidOptionalItemRecipe extends ItemStackToFluidOptionalItemRecipe {

    protected final ItemStackIngredient input;
    protected final FluidOptionalItemOutput output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicItemStackToFluidOptionalItemRecipe(ItemStackIngredient input, FluidOptionalItemOutput output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
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
    public FluidOptionalItemOutput getOutput(ItemStack input) {
        return output.copy();
    }

    public FluidOptionalItemOutput getOutputRaw() {
        return output;
    }

    @Override
    public List<FluidOptionalItemOutput> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToFluidOptionalItemRecipe other = (BasicItemStackToFluidOptionalItemRecipe) o;
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + output.hashCode();
        return result;
    }
}