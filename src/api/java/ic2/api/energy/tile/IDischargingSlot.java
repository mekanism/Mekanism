package ic2.api.energy.tile;

/**
 * Represents a slot (or set of slots) that can have electric items in them to be discharged
 *
 * @author Chocohead
 */
public interface IDischargingSlot {
	/**
	 * Called when the energy component is ticked to empty the slots
	 *
	 * @param space The amount of free space for energy that can be stored
	 * @param ignoreLimit Whether to ignore the discharge limit of the item in the slot(s)
	 * @return The amount of energy discharged from the items in the slot(s)
	 */
	public double discharge(double space, boolean ignoreLimit);
}