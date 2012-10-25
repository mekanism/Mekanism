package universalelectricity.implement;

import net.minecraftforge.common.ForgeDirection;

/**
 * Applied to tile entities that can produce electricity
 * 
 * @author Calclavia
 */
public interface IElectricityProducer extends IConnector, IDisableable, IVoltage
{
	/**
	 * Can this machine visually connect to a wire on this specific side?
	 * 
	 * @param side
	 *            . 0-5 byte
	 * @return - True if so.
	 */
	public boolean canConnect(ForgeDirection side);
}
