package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Input: ItemStack
 * <br>
 * Output: ItemStack
 *
 * @apiNote There are currently three types of ItemStack to ItemStack recipe types:
 * <ul>
 *     <li>Crushing: Can be processed in Crushers and Crushing Factories.</li>
 *     <li>Enriching: Can be processed in Enrichment Chambers and Enriching Factories.</li>
 *     <li>Smelting: Can be processed in Energized Smelters, Smelting Factories, and Robits.</li>
 * </ul>
 */
@NothingNullByDefault
public abstract class ItemStackToItemStackRecipe extends MekanismRecipe implements Predicate<@NotNull ItemStack> {

    protected final ItemStackIngredient input;
    protected final ItemStack output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public ItemStackToItemStackRecipe(ItemStackIngredient input, ItemStack output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(ItemStack input) {
        return this.input.test(input);
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
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new", pure = true)
    public ItemStack getOutput(ItemStack input) {
        return output.copy();
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
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
    public boolean isIncomplete() {
        return input.hasNoMatchingInstances();
    }

}