package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Input: ItemStack
 * <br>
 * Output: FloatingLong
 *
 * @apiNote Energy conversion recipes can be used in any slots in Mekanism machines that are able to convert items into energy.
 */
@NothingNullByDefault
public abstract class ItemStackToEnergyRecipe extends MekanismRecipe implements Predicate<@NotNull ItemStack> {

    protected final ItemStackIngredient input;
    protected final FloatingLong output;

    /**
     * @param id     Recipe name.
     * @param input  Input.
     * @param output Output, must be greater than zero.
     */
    public ItemStackToEnergyRecipe(ResourceLocation id, ItemStackIngredient input, FloatingLong output) {
        super(id);
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

    /**
     * Gets the input ingredient.
     */
    public ItemStackIngredient getInput() {
        return input;
    }

    /**
     * Gets the output based on the given input.
     *
     * @param input Specific input.
     *
     * @return Output as a constant.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    public FloatingLong getOutput(ItemStack input) {
        return output;
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<FloatingLong> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean isIncomplete() {
        return input.hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        input.write(buffer);
        output.writeToBuffer(buffer);
    }
}