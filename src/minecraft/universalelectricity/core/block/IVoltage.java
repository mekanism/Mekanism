package universalelectricity.core.block;

/**
 * Applies to all objects that has a voltage.
 * 
 * @author Calclavia
 * 
 */
public interface IVoltage
{
	/**
	 * Gets the voltage of this object.
	 * 
	 * @return The amount of volts. E.g 120v or 240v
	 */
	public double getVoltage();
}
