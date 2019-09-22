package mekanism.api.recipes;

import java.util.function.IntPredicate;
import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;

/**
 * Ambient Accumulator Recipe
 *
 * Input: int (ticks taken) Output: GasStack
 */
//TODO: Decide if this should be removed, or if I should restore all tiles and things for the ambient accumulator
public class AmbientAccumulatorRecipe implements IMekanismRecipe, IntPredicate {

    private final int ticksRequired;
    private final int dimension;
    @Nonnull
    private final GasStack output;

    public AmbientAccumulatorRecipe(int dimensionId, int ticksRequired, @Nonnull GasStack output) {
        this.dimension = dimensionId;
        this.ticksRequired = ticksRequired;
        this.output = output;
    }

    /**
     * Check dimension against recipe
     *
     * @param value the dimension Id
     *
     * @return true if match
     */
    @Override
    public boolean test(int value) {
        return value == dimension;
    }

    @Nonnull
    public GasStack getOutput() {
        return output;
    }

    public int getTicksRequired() {
        return ticksRequired;
    }

    public int getDimension() {
        return dimension;
    }
}
