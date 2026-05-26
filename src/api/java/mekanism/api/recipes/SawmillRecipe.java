package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

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
public abstract class SawmillRecipe extends ItemInputRecipe<ChanceOutput> {

    protected static final RandomSource RANDOM = RandomSource.create();
    private static final Holder<Item> PRECISION_SAWMILL = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "precision_sawmill"));

    /**
     * For JEI, gets the main output representations to display.
     *
     * @return Representation of the main output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ItemStackTemplate> getMainOutputDefinition();

    /**
     * For JEI, gets the secondary output representations to display.
     *
     * @return Representation of the secondary output, <strong>MUST NOT</strong> be modified.
     */
    public abstract List<ItemStackTemplate> getSecondaryOutputDefinition();

    @Override
    @Deprecated
    public List<ChanceOutput> getOutputDefinition() {
        //TODO - 26.1: Re-evaluate should we throw instead?
        return Collections.emptyList();
    }

    /**
     * Gets the chance (between 0 and 1) of the secondary output being produced.
     */
    public abstract double getSecondaryChance();

    @Override
    public final RecipeType<SawmillRecipe> getType() {
        return MekanismRecipeTypes.TYPE_SAWING.value();
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
         * Gets a copy of the main output of this recipe. This may be null if there is only a secondary chance based output.
         *
         */
        @Nullable
        ItemStackTemplate getMainOutput();

        /**
         * Gets a copy of the secondary output ignoring the random chance of it happening. This is mostly used for checking the maximum amount we can get as a secondary
         * output for purposes of seeing if we have space to process.
         *
         */
        @Nullable
        ItemStackTemplate getMaxSecondaryOutput();

        /**
         * Gets a copy of the secondary output if the random number generated for this output matches the chance of a secondary output being produced, otherwise returns
         * an empty stack.
         *
         * @implSpec It is expected that if this doesn't return null that the type is the same as the type returned in {@link #getMaxSecondaryOutput()}
         */
        @Nullable
        ItemStackTemplate getSecondaryOutput();

        /**
         * Similar to {@link #getSecondaryOutput()} except that this calculates a new random number to act as if this was another chance output for purposes of handling
         * multiple operations at once.
         *
         * @implNote return a new copy or {@code null}
         * @implSpec It is expected that if this doesn't return null that the type is the same as the type returned in {@link #getMaxSecondaryOutput()}
         */
        @Nullable
        ItemStackTemplate nextSecondaryOutput();
    }
}
