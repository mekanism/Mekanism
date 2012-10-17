package universalelectricity.implement;

/**
 * This interface is to be applied to all tile entities which stores electricity
 * within them.
 * @author Calclavia
 */
public interface IElectricityStorage
{
	/**
	 * Returns the amount of watt hours this unit has stored.
	 */
	public double getWattHours(Object... data);
	
	/**
	 * Sets the amount of watt hours this unit has stored.
	 */
	public void setWattHours(double wattHours, Object... data);
	
	/**
	 * Gets the maximum amount of watt hours this unit can store.
	 */
	public double getMaxWattHours();
}
