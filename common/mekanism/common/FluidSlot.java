package mekanism.common;

/**
 * Used to manage a slot that stores fluid. Has 3 main values: a stored amount of fluid,
 * maximum fluid, and fluid ID.
 * @author AidanBrady
 *
 */
public class FluidSlot
{
	/** The amount of fluid this slot is currently holding. */
	public int fluidStored;

	/** The maximum amount of fluid this slot can handle. */
	public int MAX_FLUID;

	/** The fluid's ID. */
	public int fluidID;

	/**
	 * Creates a FluidSlot with a defined fluid ID and max fluid. The fluid stored starts at 0.
	 * @param max - max fluid
	 * @param id - fluid id
	 */
	public FluidSlot(int max, int id)
	{
		MAX_FLUID = max;
		fluidID = id;
	}

	/**
	 * Sets the fluid to a new amount.
	 * @param fluid - amount to store
	 */
	public void setFluid(int amount)
	{
		fluidStored = Math.max(Math.min(amount, MAX_FLUID), 0);
	}
}
