package mekanism.common.multipart;

import java.util.Collection;

import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.DynamicNetwork.NetworkClientRequest;
import mekanism.api.transmitters.ITransmitterTile;
import mekanism.api.transmitters.TransmitterNetworkRegistry;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class PartTransmitter<A, N extends DynamicNetwork<A, N>> extends PartSidedPipe implements ITransmitterTile<A, N>
{
	public MultipartTransmitter<A, N> transmitterDelegate;

	public PartTransmitter()
	{
		transmitterDelegate = new MultipartTransmitter<>(this);
	}

	@Override
	public MultipartTransmitter<A, N> getTransmitter()
	{
		return transmitterDelegate;
	}

	@Override
	public void onWorldJoin()
	{
		super.onWorldJoin();
		
		if(!world().isRemote)
		{
			TransmitterNetworkRegistry.registerOrphanTransmitter(getTransmitter());
		}
		else {
			MinecraftForge.EVENT_BUS.post(new NetworkClientRequest(tile()));
		}
	}

	public abstract N createNewNetwork();

	public abstract N createNetworkByMerging(Collection<N> networks);

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		if(!world().isRemote)
		{
			getTransmitter().takeShare();
			TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
		}
		else {
			getTransmitter().setTransmitterNetwork(null);
		}
	}

	@Override
	public void preRemove()
	{
		if(!world().isRemote)
		{
			TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
		}
		else {
			getTransmitter().setTransmitterNetwork(null);
		}
		
		super.preRemove();
	}

	@Override
	public void onNeighborTileChanged(int side, boolean weak)
	{
		super.onNeighborTileChanged(side, weak);
	}

	@Override
	public void markDirtyTransmitters()
	{
		super.markDirtyTransmitters();
		
		if(getTransmitter().hasTransmitterNetwork())
		{
			TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
		}
	}

	@Override
	public void markDirtyAcceptor(ForgeDirection side)
	{
		super.markDirtyAcceptor(side);
		
		if(getTransmitter().hasTransmitterNetwork())
		{
			getTransmitter().getTransmitterNetwork().acceptorChanged(getTransmitter(), side);
		}
	}

	public A getCachedAcceptor(ForgeDirection side)
	{
		ConnectionType type = connectionTypes[side.ordinal()];
		
		if(type == ConnectionType.PULL || type == ConnectionType.NONE)
		{
			return null;
		}
		
		return connectionMapContainsSide(currentAcceptorConnections, side) ? (A)cachedAcceptors[side.ordinal()] : null;
	}

	public abstract int getCapacity();

	public abstract Object getBuffer();

	public abstract void takeShare();
}
