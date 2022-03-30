package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

/**
 * Base class for defining ItemStack to fluid recipes.
 * <br>
 * Input: ItemStack
 * <br>
 * Output: FluidStack
 *
 * @apiNote There is currently only one type of ItemStack to FluidStack recipe type:
 * <ul>
 *     <li>Nutritional Liquification: These cannot currently be created, but are processed in the Nutritional Liquifier.</li>
 * </ul>
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackToFluidRecipe extends MekanismRecipe implements Predicate<@NonNull ItemStack> {

    protected final ItemStackIngredient input;
    protected final FluidStack output;

    /**
     * @param id     Recipe name.
     * @param input  Input.
     * @param output Output.
     */
    public ItemStackToFluidRecipe(ResourceLocation id, ItemStackIngredient input, FluidStack output) {
        super(id);
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

    /**
     * Gets the input ingredient.
     */
    public ItemStackIngredient getInput() {
        return input;
    }

    /**
     * Gets a new output based on the given input.
     *
     * @param input Specific input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public FluidStack getOutput(ItemStack input) {
        return output.copy();
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<FluidStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean isIncomplete() {
        return input.hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        input.write(buffer);
        output.writeToPacket(buffer);
    }
}