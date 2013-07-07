package ic2.api.energy.tile;

import ic2.api.Direction;

/**
 * Allows a tile entity (mostly a machine) to receive energy.
 */
public interface IEnergySink extends IEnergyAcceptor {
	/**
	 * Determine how much energy the sink accepts.
	 *
	 * This value is unrelated to getMaxSafeInput().
	 *
	 * Make sure that injectEnergy() does accepts energy if demandsEnergy() returns anything > 0.
	 *
	 * @return max accepted input in eu
	 */
	int demandsEnergy();

	/**
	 * Transfer energy to the sink.
	 *
	 * @param directionFrom direction from which the energy comes from
	 * @param amount energy to be transferred
	 * @return Energy not consumed (leftover)
	 */
	int injectEnergy(Direction directionFrom, int amount);

	/**
	 * Determine the amount of eu which can be safely injected into the specific energy sink without exploding.
	 *
	 * Typical values are 32 for LV, 128 for MV, 512 for HV and 2048 for EV. A value of Integer.MAX_VALUE indicates no
	 * limit.
	 *
	 * This value is unrelated to demandsEnergy().
	 *
	 * @return max safe input in eu
	 */
	int getMaxSafeInput();
}

