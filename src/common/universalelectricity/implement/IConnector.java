package universalelectricity.implement;

import net.minecraftforge.common.ForgeDirection;

/**
 * Applied to TileEntities that can connect to UE wires.
 * 
 * @author Calclavia
 * 
 */
public interface IConnector
{
	/**
	 * Can this tile entity visually connect to a wire on this specific side?
	 * 
	 * @param side
	 *            - The side in which the connection is coming from.
	 * @return - True if so.
	 */
	public boolean canConnect(ForgeDirection side);
}
