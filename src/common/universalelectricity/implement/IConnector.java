package universalelectricity.implement;

import net.minecraftforge.common.ForgeDirection;

/**
 * Applied to tile entities that can connect to UE wires.
 * @author Calclavia
 *
 */
public interface IConnector
{
    /**
     * Can this tile entity visually connect to a wire on this specific side?
     * @param side. 0-5 byte
     * @return - True if so.
     */
    public boolean canConnect(ForgeDirection side);
}
