package universalelectricity.core.block;

/**
 * This interface is to be applied to all TileEntities which stores electricity within them.
 * 
 * @author Calclavia
 */
public interface IElectricityStorage
{
	/**
	 * Returns the amount of joules this unit has stored.
	 */
	public double getJoules();

	/**
	 * Sets the amount of joules this unit has stored.
	 */
	public void setJoules(double joules);

	/**
	 * Gets the maximum amount of joules this unit can store.
	 */
	public double getMaxJoules();
}
