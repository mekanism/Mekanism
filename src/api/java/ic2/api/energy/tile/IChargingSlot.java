package ic2.api.energy.tile;

/**
 * Represents a slot (or set of slots) that can have electric items in them to be charged
 *
 * @author Chocohead
 */
public interface IChargingSlot {
	/**
	 * Called when the energy component is ticked to fill the slots
	 *
	 * @param amount The amount of energy available for the slot(s)
	 * @return The amount of energy used charging the items in the slot(s)
	 */
	public double charge(double amount);
}