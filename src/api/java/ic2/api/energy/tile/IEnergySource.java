package ic2.api.energy.tile;

/**
 * Allows a tile entity (mostly a generator) to emit energy.
 *
 * See ic2/api/energy/usage.txt for an overall description of the energy net api.
 */
public interface IEnergySource extends IEnergyEmitter {
	/**
	 * Maximum energy output provided by the source this tick, typically the stored energy.
	 *
	 * <p>The value will be limited to the source tier's power multiplied with the packet count
	 * (see {@link IMultiEnergySource}, default 1).
	 *
	 * @note Modifying the energy net from this method is disallowed.
	 *
	 * @return Energy offered this tick
	 */
	double getOfferedEnergy();

	/**
	 * Draw energy from this source's buffer.
	 *
	 * <p>If the source doesn't have a buffer, this may be a no-op.
	 *
	 * @param amount amount of EU to draw, may be negative
	 */
	void drawEnergy(double amount);

	/**
	 * Determine the tier of this energy source.
	 * 1 = LV, 2 = MV, 3 = HV, 4 = EV etc.
	 *
	 * @note Modifying the energy net from this method is disallowed.
	 *
	 * @return tier of this energy source
	 */
	int getSourceTier();
}

