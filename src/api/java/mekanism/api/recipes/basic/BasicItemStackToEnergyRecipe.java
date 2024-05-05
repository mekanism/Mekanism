package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class BasicItemStackToEnergyRecipe extends ItemStackToEnergyRecipe {

    protected final ItemStackIngredient input;
    protected final FloatingLong output;

    /**
     * @param input  Input.
     * @param output Output, must be greater than zero.
     */
    public BasicItemStackToEnergyRecipe(ItemStackIngredient input, FloatingLong output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isZero()) {
            throw new IllegalArgumentException("Output must be greater than zero.");
        }
        //Ensure that the floating long we are storing is immutable
        this.output = output.copyAsConst();
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
    public FloatingLong getOutput(ItemStack input) {
        return output;
    }

    /**
     * For Serializer use. DO NOT MODIFY RETURN VALUE.
     *
     * @return the uncopied basic output
     *
     * @since 10.6.0
     */
    public FloatingLong getOutputRaw() {
        return output;
    }

    @Override
    public List<FloatingLong> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public RecipeSerializer<BasicItemStackToEnergyRecipe> getSerializer() {
        return MekanismRecipeSerializers.ENERGY_CONVERSION.get();
    }
}