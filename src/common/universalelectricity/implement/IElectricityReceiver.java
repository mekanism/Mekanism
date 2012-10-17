package universalelectricity.implement;

import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;


/**
 * The IMachine interface is an interface that must be applied to all tile entities that can input or output electricity.
 * @author Calclavia
 *
 */
public interface IElectricityReceiver extends IDisableable, IConnector, IVoltage
{
    /**
     * Called every tick on this machine.
     * 
     * @param amps - Amount of amps this electric unit is receiving.
     * @param voltage - The voltage of the electricity sent. If more than one
     * packet is being sent to you in this update, the highest voltage will
     * override.
     * @param side - The side of the block in which the electricity is coming from.
     */
    public void onReceive(TileEntity sender, double amps, double voltage, ForgeDirection side);

    /**
     * How many watts does this electrical unit need per tick?
     * Recommend for you to return the max electricity storage of this machine (if there is one).
     *
     * Set this to 0 if your electric unit can not receive electricity.
     */
    public double wattRequest();

    /**
     * Can this unit receive electricity from this specific side
     * @param side. 0-5 byte
     * @return - True if so.
     */
    public boolean canReceiveFromSide(ForgeDirection side);
}