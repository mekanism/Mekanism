package mekanism.common.base;

import net.minecraftforge.fluids.FluidStack;

/**
 * Internal interface used in blocks and items that are capable of storing sustained tanks.
 *
 * @author AidanBrady
 */
public interface ISustainedTank {

    /**
     * Sets the tank tag list to a new value.
     *
     * @param fluidStack - Fluidstack to apply to
     * @param data       - ItemStack parameter if using on item
     */
    void setFluidStack(FluidStack fluidStack, Object... data);

    /**
     * Gets the tank tag list from an item or block.
     *
     * @param data - ItemStack parameter if using on item
     *
     * @return inventory tag list
     */
    FluidStack getFluidStack(Object... data);

    /**
     * Whether or not this block or item has an internal tank.
     *
     * @param data - ItemStack parameter if using on item
     *
     * @return if the block or item has an internal tank
     */
    boolean hasTank(Object... data);
}
