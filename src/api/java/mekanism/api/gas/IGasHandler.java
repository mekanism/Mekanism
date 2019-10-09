package mekanism.api.gas;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import net.minecraft.util.Direction;

/**
 * Implement this if your tile entity accepts gas from an external source.
 *
 * @author AidanBrady
 */
//TODO: Figure out if Side in the various methods *can* be null, if not mark it as nonnull, or better yet mark everything here as nonnull by default
public interface IGasHandler {

    GasTankInfo[] NONE = new GasTankInfo[0];

    /**
     * Transfer a certain amount of gas to this block.
     *
     * @param stack - gas to add
     *
     * @return gas added
     */
    int receiveGas(Direction side, @Nonnull GasStack stack, Action action);

    /**
     * Draws a certain amount of gas from this block.
     *
     * @param amount - amount to draw
     *
     * @return gas drawn
     */
    //TODO: Should we have a method for drawing based on stack?
    @Nonnull
    GasStack drawGas(Direction side, int amount, Action action);

    /**
     * Whether or not this block can accept gas from a certain side.
     *
     * @param side - side to check
     * @param type - type of gas to check
     *
     * @return if block accepts gas
     */
    boolean canReceiveGas(Direction side, @Nonnull Gas type);

    /**
     * Whether or not this block can be drawn of gas from a certain side.
     *
     * @param side - side to check
     * @param type - type of gas to check
     *
     * @return if block can be drawn of gas
     */
    //TODO: How to handle checking "ANY" Type, pass empty gas type. Needs to support it properly again
    boolean canDrawGas(Direction side, @Nonnull Gas type);

    /**
     * Gets the tanks present on this handler. READ ONLY. DO NOT MODIFY.
     *
     * @return an array of GasTankInfo elements corresponding to all tanks.
     */
    @Nonnull
    default GasTankInfo[] getTankInfo() {
        return NONE;
    }
}