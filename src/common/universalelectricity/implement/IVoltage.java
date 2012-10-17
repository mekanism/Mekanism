package universalelectricity.implement;

public interface IVoltage
{
    /**
     * Gets the voltage of the electricity consumer. Used in a conductor to find the potential difference.
     * If the voltage is too high, things might explode.
     * @return The amount of volts. E.g 120v or 240v
     */
    public double getVoltage();
}
