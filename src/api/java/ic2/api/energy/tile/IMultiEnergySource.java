package ic2.api.energy.tile;

/**
 * Allows an {@link IEnergySource} to emit more than one EU packet per tick.
 * Note: support for this will be dropped in a future version of IC2.
 * @author Aroma1997
 */
public interface IMultiEnergySource extends IEnergySource
{

	/**
	 * If you want your machine to emit more than one EU packets per tick return true here.
	 * @return if the machine in its current state can emit more than one EU packet per tick.
	 */
	public boolean sendMultipleEnergyPackets();

	/**
	 * If {@link #sendMultipleEnergyPackets()} returned true, this will get called.
	 * The value returned by this method will determine the amount of EU packets, this machine can emit.
	 * @return the amount of EU packets, this machine can emit per tick.
	 */
	public int getMultipleEnergyPacketAmount();

}