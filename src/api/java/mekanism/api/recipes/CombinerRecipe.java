package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;

/**
 * Main Input: ItemStack
 * <br>
 * Secondary/Extra Input: ItemStack
 * <br>
 * Output: ItemStack
 *
 * @apiNote Combiners and Combining Factories can process this recipe type.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CombinerRecipe extends MekanismRecipe implements BiPredicate<@NonNull ItemStack, @NonNull ItemStack> {

    private final ItemStackIngredient mainInput;
    private final ItemStackIngredient extraInput;
    private final ItemStack output;

    /**
     * @param id         Recipe name.
     * @param mainInput  Main input.
     * @param extraInput Secondary/extra input.
     * @param output     Output.
     */
    public CombinerRecipe(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        super(id);
        this.mainInput = Objects.requireNonNull(mainInput, "Main input cannot be null.");
        this.extraInput = Objects.requireNonNull(extraInput, "Secondary/Extra input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(@Nonnull ItemStack input, @Nonnull ItemStack extra) {
        return mainInput.test(input) && extraInput.test(extra);
    }

    /**
     * Gets the main input ingredient.
     */
    public ItemStackIngredient getMainInput() {
        return mainInput;
    }

    /**
     * Gets the secondary input ingredient.
     */
    public ItemStackIngredient getExtraInput() {
        return extraInput;
    }

    /**
     * Gets a new output based on the given inputs.
     *
     * @param input Specific input.
     * @param extra Specific secondary/extra input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(@Nonnull ItemStack input, @Nonnull ItemStack extra) {
        return output.copy();
    }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public void write(PacketBuffer buffer) {
        mainInput.write(buffer);
        extraInput.write(buffer);
        buffer.writeItem(output);
    }
}