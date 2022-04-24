package mekanism.api.recipes;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;

/**
 * Input: ItemStack
 * <br>
 * Primary Output: ItemStack (can be empty if secondary output is not empty)
 * <br>
 * Secondary Output: Chance based ItemStack (can be empty/zero chance if primary output is not empty)
 *
 * @apiNote Precision Sawmills and Sawing Factories can process this recipe type.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SawmillRecipe extends MekanismRecipe implements Predicate<@NonNull ItemStack> {

    protected static final Random RANDOM = new Random();

    private final ItemStackIngredient input;
    private final ItemStack mainOutput;
    private final ItemStack secondaryOutput;
    private final double secondaryChance;

    /**
     * @param id              Recipe name.
     * @param input           Input.
     * @param mainOutput      Main Output.
     * @param secondaryOutput Secondary Output (chance based).
     * @param secondaryChance Chance of the secondary output being produced. This must be at least zero and at most one.
     *
     * @apiNote At least one output must not be empty.
     */
    public SawmillRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        super(id);
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(mainOutput, "Main output cannot be null.");
        Objects.requireNonNull(secondaryOutput, "Secondary output cannot be null.");
        if (mainOutput.isEmpty() && secondaryOutput.isEmpty()) {
            throw new IllegalArgumentException("At least one output must not be empty.");
        } else if (secondaryChance < 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("Secondary output chance must be at least zero and at most one.");
        } else if (mainOutput.isEmpty()) {
            if (secondaryChance == 0 || secondaryChance == 1) {
                throw new IllegalArgumentException("Secondary output must have a chance greater than zero and less than one.");
            }
        } else if (secondaryOutput.isEmpty() && secondaryChance != 0) {
            throw new IllegalArgumentException("If there is no secondary output, the chance of getting the secondary output should be zero.");
        }
        this.mainOutput = mainOutput.copy();
        this.secondaryOutput = secondaryOutput.copy();
        this.secondaryChance = secondaryChance;
    }

    @Override
    public boolean test(ItemStack stack) {
        return this.input.test(stack);
    }

    /**
     * Gets a new chance output based on the given input.
     *
     * @param input Specific input.
     *
     * @return New chance output.
     *
     * @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in input should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_ -> new")
    public ChanceOutput getOutput(ItemStack input) {
        return new ChanceOutput(secondaryChance > 0 ? RANDOM.nextDouble() : 0);
    }

    /**
     * For JEI, gets the main output representations to display.
     *
     * @return Representation of the main output, <strong>MUST NOT</strong> be modified.
     */
    public List<ItemStack> getMainOutputDefinition() {
        return mainOutput.isEmpty() ? Collections.emptyList() : Collections.singletonList(mainOutput);
    }

    /**
     * For JEI, gets the secondary output representations to display.
     *
     * @return Representation of the secondary output, <strong>MUST NOT</strong> be modified.
     */
    public List<ItemStack> getSecondaryOutputDefinition() {
        return secondaryOutput.isEmpty() ? Collections.emptyList() : Collections.singletonList(secondaryOutput);
    }

    /**
     * Gets the chance (between 0 and 1) of the secondary output being produced.
     */
    public double getSecondaryChance() {
        return secondaryChance;
    }

    /**
     * Gets the input ingredient.
     */
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    public boolean isIncomplete() {
        return input.hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        input.write(buffer);
        buffer.writeItem(mainOutput);
        buffer.writeItem(secondaryOutput);
        buffer.writeDouble(secondaryChance);
    }

    /**
     * Represents a precalculated chance based output. This output keeps track of what random value was calculated for use in comparing if the secondary output should be
     * created.
     */
    public class ChanceOutput {

        protected final double rand;

        protected ChanceOutput(double rand) {
            this.rand = rand;
        }

        /**
         * Gets a copy of the main output of this recipe. This may be empty if there is only a secondary chance based output.
         */
        public ItemStack getMainOutput() {
            return mainOutput.copy();
        }

        /**
         * Gets a copy of the secondary output ignoring the random chance of it happening. This is mostly used for checking the maximum amount we can get as a secondary
         * output for purposes of seeing if we have space to process.
         */
        public ItemStack getMaxSecondaryOutput() {
            return secondaryChance > 0 ? secondaryOutput.copy() : ItemStack.EMPTY;
        }

        /**
         * Gets a copy of the secondary output if the random number generated for this output matches the chance of a secondary output being produced, otherwise returns
         * an empty stack.
         */
        public ItemStack getSecondaryOutput() {
            if (rand <= secondaryChance) {
                return secondaryOutput.copy();
            }
            return ItemStack.EMPTY;
        }

        /**
         * Similar to {@link #getSecondaryOutput()} except that this calculates a new random number to act as if this was another chance output for purposes of handling
         * multiple operations at once.
         */
        public ItemStack nextSecondaryOutput() {
            if (secondaryChance > 0) {
                double rand = RANDOM.nextDouble();
                if (rand <= secondaryChance) {
                    return secondaryOutput.copy();
                }
            }
            return ItemStack.EMPTY;
        }
    }
}