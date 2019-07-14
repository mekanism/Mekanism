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

    private final GasStack output;

    public AmbientAccumulatorRecipe(int ticksRequired, GasStack output) {
        this.ticksRequired = ticksRequired;
        this.output = output;
    }

    @Override
    public boolean test(int value) {
        return value >= ticksRequired;
    }

    public GasStack getOutput() {
        return output;
    }

    public int getTicksRequired() {
        return ticksRequired;
    }
}
