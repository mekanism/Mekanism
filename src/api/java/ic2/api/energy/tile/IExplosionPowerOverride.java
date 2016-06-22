package ic2.api.energy.tile;

/**
 * Implement with an {@link IEnergySink} tile entity to change the force of the explosion when overpowered.
 */
public interface IExplosionPowerOverride {

	/**
	 * Checks if the electric tile should explode. If you return false here,
	 * the explosion will just not happen at all and the block will stay.
	 * This is NOT recommended.
	 * @return Whether the block should explode.
	 *
	 * @note It should not be dependent on the tier of the power injected, consider {@link IOverloadHandler} if necessary.
	 */
	boolean shouldExplode();

	/**
	 * The explosion force when too much power is received.
	 * @param tier The tier of power, that was injected into the energy sink, that caused it to explode.
	 * @param defaultPower The default explosion power.
	 * @return The explosion power
	 */
	float getExplosionPower(int tier, float defaultPower);
}