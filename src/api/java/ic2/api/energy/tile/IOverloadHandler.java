package ic2.api.energy.tile;

/**
 * Complete control for when an {@link IEnergySink} is overloaded.
 */
public interface IOverloadHandler {

	/**
	 * Called when overloaded. This will override the default explosion.
	 * The default explosion will therefore NOT happen, when you return true here.
	 * If you return false, you can use this as a onExplode hook, but still let the default explosion happen.
	 * @note Will set the block to air and cause an explosion of power 2.5 if false is returned
	 * @param tier The tier of power, that was injected into the energy sink, that caused it to explode.
	 * @return Whether the default explosion should be cancelled.
	 */
	boolean onOverload(int tier);
}