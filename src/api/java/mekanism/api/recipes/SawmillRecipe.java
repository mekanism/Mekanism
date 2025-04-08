package mekanism.api.recipes;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Input: ItemStack
 * <br>
 * Primary Output: ItemStack (can be empty if secondary output is not empty)
 * <br>
 * Secondary Output: Chance based ItemStack (can be empty/zero chance if primary output is not empty)
 *
 * @apiNote Precision Sawmills and Sawing Factories can process this recipe type.
 */
@NothingNullByDefault
public abstract class SawmillRecipe extends MekanismRecipe<SingleRecipeInput> implements Predicate<@NotNull ItemStack> {

    protected static final RandomSource RANDOM = RandomSource.create();
    private static final Holder<Item> PRECISION_SAWMILL = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "precision_sawmill"));

    @Override
    public abstract boolean test(ItemStack stack);

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item());
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
    @Contract("_ -> new")
    public abstract ChanceOutput getOutput(ItemStack input);

    /**
     * For JEI, gets the main output representations to display.
     *
     * @return Representation of the main output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ItemStack> getMainOutputDefinition();

    /**
     * For JEI, gets the secondary output representations to display.
     *
     * @return Representation of the secondary output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ItemStack> getSecondaryOutputDefinition();

    /**
     * Gets the chance (between 0 and 1) of the secondary output being produced.
     */
    public abstract double getSecondaryChance();

    /**
     * Gets the input ingredient.
     */
    public abstract ItemStackIngredient getInput();

    @Override
    public boolean isIncomplete() {
        return getInput().hasNoMatchingInstances();
    }

    @Override
    public void logMissingTags() {
        getInput().logMissingTags();
    }

    @Override
    public final RecipeType<SawmillRecipe> getType() {
        return MekanismRecipeTypes.TYPE_SAWING.value();
    }

    @Override
    public String getGroup() {
        return "precision_sawmill";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PRECISION_SAWMILL);
    }

    /**
     * Represents a precalculated chance based output. This output keeps track of what random value was calculated for use in comparing if the secondary output should be
     * created.
     */
    public interface ChanceOutput {

        /**
         * Gets a copy of the main output of this recipe. This may be empty if there is only a secondary chance based output.
         *
         * @implNote return a new copy
         */
        ItemStack getMainOutput();

        /**
         * Gets a copy of the secondary output ignoring the random chance of it happening. This is mostly used for checking the maximum amount we can get as a secondary
         * output for purposes of seeing if we have space to process.
         *
         * @implNote return a new copy or ItemStack.EMPTY
         */
        ItemStack getMaxSecondaryOutput();

        /**
         * Gets a copy of the secondary output if the random number generated for this output matches the chance of a secondary output being produced, otherwise returns
         * an empty stack.
         *
         * @implNote return a new copy or ItemStack.EMPTY
         */
        ItemStack getSecondaryOutput();

        /**
         * Similar to {@link #getSecondaryOutput()} except that this calculates a new random number to act as if this was another chance output for purposes of handling
         * multiple operations at once.
         *
         * @implNote return a new copy or ItemStack.EMPTY
         */
        ItemStack nextSecondaryOutput();
    }
}
