package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public abstract class BasicItemStackToFluidRecipe extends ItemStackToFluidRecipe {

    protected final ItemStackIngredient input;
    protected final FluidStackTemplate output;

    /// @param input  Input.
    /// @param output Output.
    public BasicItemStackToFluidRecipe(ItemStackIngredient input, FluidStackTemplate output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    @Contract(pure = true)
    public FluidStackTemplate getOutput(TypedInstance<Item> input) {
        return output;
    }

    @Override
    public List<FluidStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToFluidRecipe other = (BasicItemStackToFluidRecipe) o;
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + output.hashCode();
        return hash;
    }
}