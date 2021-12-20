package mekanism.api.recipes;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Input: ItemStack
 * <br>
 * Output: FloatingLong
 *
 * @apiNote Energy conversion recipes can be used in any slots in Mekanism machines that are able to convert items into energy.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToEnergyRecipe extends MekanismRecipe implements Predicate<@NonNull ItemStack> {

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
    public FloatingLong getOutputDefinition() {
        //TODO - 1.18: Re-evaluate this method not being a list
        return output;
    }

    @Override
    public void write(PacketBuffer buffer) {
        input.write(buffer);
        output.writeToBuffer(buffer);
    }
}