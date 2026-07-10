package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.jspecify.annotations.Nullable;

public class BasicItemStackToEnergyRecipe extends ItemStackToEnergyRecipe {

    protected final ItemStackIngredient input;
    protected final int output;

    /// @param input  Input.
    /// @param output Output, must be greater than zero.
    public BasicItemStackToEnergyRecipe(ItemStackIngredient input, int output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        if (output <= 0) {
            throw new IllegalArgumentException("Output must be greater than zero.");
        }
        this.output = output;
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
    public int getOutput(ItemStack input) {
        return output;
    }

    /// For Serializer use. DO NOT MODIFY RETURN VALUE.
    ///
    /// @return the uncopied basic output
    ///
    /// @since 10.6.0
    public int getOutputRaw() {
        return output;
    }

    @Override
    public int[] getOutputDefinition(ContextMap contextMap) {
        return new int[]{output};
    }

    @Override
    public RecipeSerializer<BasicItemStackToEnergyRecipe> getSerializer() {
        return MekanismRecipeSerializers.ENERGY_CONVERSION.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackToEnergyRecipe other = (BasicItemStackToEnergyRecipe) o;
        return output == other.output && input.equals(other.input);
    }

    @Override
    public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + output;
        return result;
    }

    @Override
    public List<RecipeDisplay> display() {
        return Collections.emptyList();
    }
}