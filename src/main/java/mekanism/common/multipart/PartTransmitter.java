package mekanism.common.multipart;

import java.util.Collection;

import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.ITransmitterTile;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
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
	}

	@Override
	public void preRemove()
	{
		if(!world().isRemote)
		{
			TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
		}
		super.preRemove();
	}

	@Override
	protected void onModeChange(ForgeDirection side)
	{
		super.onModeChange(side);
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
		return connectionMapContainsSide(currentAcceptorConnections, side) ? (A)cachedAcceptors[side.ordinal()] : null;
	}

	public abstract int getCapacity();

	public abstract Object getBuffer();

	public abstract void takeShare();
}
