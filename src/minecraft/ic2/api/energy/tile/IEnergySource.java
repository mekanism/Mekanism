package ic2.api.energy.tile;

/**
 * Allows a tile entity (mostly a generator) to emit energy.
 */
public interface IEnergySource extends IEnergyEmitter {
	/**
	 * Maximum energy output provided by the source.
	 * If unsure, use Integer.MAX_VALUE.
	 * 
	 * @return Maximum energy output
	 */
	int getMaxEnergyOutput();
}

