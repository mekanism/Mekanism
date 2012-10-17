package universalelectricity.implement;

import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * Must be applied to all tile entities that are conductors.
 * @author Calclavia
 *
 */
public interface IConductor extends IConnector
{
	/**
	 * The ID of the connection this conductor is in.
	 */
	public int getConnectionID();
	
	public void setConnectionID(int ID);
	
	/**
	 * The UE tile entities that this conductor is connected to.
	 * @return
	 */
    public TileEntity[] getConnectedBlocks();
	
	/**
     * Gets the resistance of the conductor. Used to calculate energy loss.
     * A higher resistance means a higher energy loss.
     * @return The amount of Ohm's of resistance.
     */
    public double getResistance();
    
    /**
     * The maximum amount of amps this conductor can handle before melting down. This is calculating
     * PER TICK!
     * @return The amount of amps in volts
     */
    public double getMaxAmps();
    
    /**
     * Called when the electricity passing through exceeds the maximum voltage.
     */
    public void onConductorMelt();
    
    /**
     * Resets the conductor and recalculate connection IDs again
     */
    public void reset();
    
    public World getWorld();

	public void updateConnectionWithoutSplit(TileEntity connectorFromSide, ForgeDirection orientation);

	public void refreshConnectedBlocks();
}
