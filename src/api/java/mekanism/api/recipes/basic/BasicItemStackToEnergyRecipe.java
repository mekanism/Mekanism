package mekanism.api.recipes.basic;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class BasicItemStackToEnergyRecipe extends ItemStackToEnergyRecipe {

    protected final ItemStackIngredient input;
    protected final long output;

    /**
     * @param input  Input.
     * @param output Output, must be greater than zero.
     */
    public BasicItemStackToEnergyRecipe(ItemStackIngredient input, long output) {
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
    public long getOutput(ItemStack input) {
        return output;
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output
     *
     * @since 10.6.0
     */
    public long getOutputRaw() {
        return output;
    }

    @Override
    public long[] getOutputDefinition() {
        return new long[]{output};
    }

    @Override
    public RecipeSerializer<BasicItemStackToEnergyRecipe> getSerializer() {
        return MekanismRecipeSerializers.ENERGY_CONVERSION.get();
    }

    @Override
    public boolean equals(Object o) {
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
        result = 31 * result + Long.hashCode(output);
        return result;
    }
}