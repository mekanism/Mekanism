package ic2.api;

/**
 * Allows a tile entity (mostly a machine) to receive energy.
 */
public interface IEnergySink extends IEnergyAcceptor {
	/**
	 * Determine whether the sink requires energy.
	 * 
	 * @return Whether the sink is requiring energy
	 */
	boolean demandsEnergy();

	/**
	 * Transfer energy to the sink.
	 *
	 * @param directionFrom direction from which the energy comes from
	 * @param amount energy to be transferred
	 * @return Energy not consumed (leftover)
	 */
	int injectEnergy(Direction directionFrom, int amount);
}

