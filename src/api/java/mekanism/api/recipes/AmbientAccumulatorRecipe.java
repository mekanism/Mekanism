package mekanism.api.recipes;

import java.util.function.IntPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.gas.GasStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

/**
 * Ambient Accumulator Recipe
 *
 * Input: int (ticks taken) Output: GasStack
 */
//TODO: Decide if this should be removed, or if I should restore all tiles and things for the ambient accumulator
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public abstract class AmbientAccumulatorRecipe extends MekanismRecipe implements IntPredicate {

    private final int ticksRequired;
    private final int dimension;
    private final GasStack output;

    public AmbientAccumulatorRecipe(ResourceLocation id, int dimensionId, int ticksRequired, GasStack output) {
        super(id);
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

    public GasStack getOutput() {
        return output;
    }

    public int getTicksRequired() {
        return ticksRequired;
    }

    public int getDimension() {
        return dimension;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(dimension);
        buffer.writeInt(ticksRequired);
        output.writeToPacket(buffer);
    }
}