package universalelectricity.prefab.implement;

/**
 * This class should be applied to all tile entities (mainly machines) that can be disabled (by
 * things like EMP, short circuit etc.).
 * 
 * @author Calclavia
 * 
 */
public interface IDisableable
{
	/**
	 * This is called when the tile entity is to be disabled.
	 * 
	 * @param duration - The duration of the disable in ticks.
	 */
	public void onDisable(int duration);

	/**
	 * Called to see if this tile entity is disabled.
	 * 
	 * @return True if the tile entity is disabled.
	 */
	public boolean isDisabled();
}
