package universalelectricity.prefab.implement;

/**
 * This interface should be applied to all things that has a tier/level.
 * 
 * @author Calclavia
 * 
 */
public interface ITier
{
	/**
	 * Gets the tier of this object
	 * 
	 * @return - The tier
	 */
	public int getTier();

	/**
	 * Sets the tier of the object
	 * 
	 * @param tier - The tier to be set
	 */
	public void setTier(int tier);
}
