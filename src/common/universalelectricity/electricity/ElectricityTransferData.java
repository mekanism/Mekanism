package universalelectricity.electricity;

import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.implement.IElectricityReceiver;

public class ElectricityTransferData
{
	public TileEntity sender;
	public IElectricityReceiver receiver;
	public ElectricityNetwork network;
	public double amps;
	public double voltage;
	public ForgeDirection side;

	/**
	 * @param sender
	 *            - Tile that's sending electricity.
	 * @param receiver
	 *            - Receiver that's receiving electricity
	 * @param conductor
	 *            - Conductor that is conducting the electricity
	 * @param side
	 *            -
	 * @param amps
	 * @param voltage
	 */
	public ElectricityTransferData(TileEntity sender, IElectricityReceiver receiver, ElectricityNetwork network, ForgeDirection side, double amps, double voltage)
	{
		this.sender = sender;
		this.receiver = receiver;
		this.network = network;
		this.side = side;
		this.amps = amps;
		this.voltage = voltage;
	}

	public boolean isValid()
	{
		return this.sender != null && this.receiver != null && this.network != null && this.amps > 0 && this.voltage > 0;
	}
}
