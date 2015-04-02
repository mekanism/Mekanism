package mekanism.api.transmitters;

import java.util.Collection;

import mekanism.api.Coord4D;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IGridTransmitter<A, N extends DynamicNetwork<A, N>> extends ITransmitter
{
	public boolean hasTransmitterNetwork();

	/**
	 * Gets the network currently in use by this transmitter segment.
	 * @return network this transmitter is using
	 */
	public N getTransmitterNetwork();

	/**
	 * Sets this transmitter segment's network to a new value.
	 * @param network - network to set to
	 */
	public void setTransmitterNetwork(N network);

	public int getTransmitterNetworkSize();

	public int getTransmitterNetworkAcceptorSize();

	public String getTransmitterNetworkNeeded();

	public String getTransmitterNetworkFlow();

	public String getTransmitterNetworkBuffer();

	public double getTransmitterNetworkCapacity();

	public int getCapacity();

	public World world();
	
	public Coord4D coord();

	public Coord4D getAdjacentConnectableTransmitterCoord(ForgeDirection side);

	public A getAcceptor(ForgeDirection side);

	public boolean isValid();

	public boolean isOrphan();

	public void setOrphan(boolean orphaned);

	public N createEmptyNetwork();

	public N mergeNetworks(Collection<N> toMerge);

	public N getExternalNetwork(Coord4D from);

	public void takeShare();

	public Object getBuffer();
}
