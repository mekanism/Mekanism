package mekanism.api.recipes;

import java.util.function.IntPredicate;
import mekanism.api.gas.GasStack;

/**
 * Ambient Accumulator Recipe
 *
 * Input: int (ticks taken)
 * Output: GasStack
 */
public class AmbientAccumulatorRecipe implements IntPredicate {

    private final int ticksRequired;

    private final int dimension;

    private final GasStack output;

    public AmbientAccumulatorRecipe(int dimensionId, int ticksRequired, GasStack output) {
        this.dimension = dimensionId;
        this.ticksRequired = ticksRequired;
        this.output = output;
    }

    /**
     * Check dimension against recipe
     * @param value the dimension Id
     * @return true if match
     */
    @Override
    public boolean test(int value) {
        return value == dimension;
    }

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
