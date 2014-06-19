package mekanism.common.multipart;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.client.ClientTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import mekanism.common.network.PacketTransmitterUpdate.TransmitterUpdateMessage;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.multipart.TileMultipart;

public abstract class PartTransmitter<N extends DynamicNetwork<?, N>> extends PartSidedPipe implements IGridTransmitter<N>, IConfigurable
{
	public N theNetwork;

	@Override
	public void bind(TileMultipart t)
	{
		if(tile() != null && theNetwork != null)
		{
			getTransmitterNetwork().transmitters.remove(tile());
			super.bind(t);
			getTransmitterNetwork().transmitters.add((IGridTransmitter<N>)tile());
		}
		else {
			super.bind(t);
		}
	}

	@Override
	public void refreshTransmitterNetwork()
	{
		byte possibleTransmitters = getPossibleTransmitterConnections();
		byte possibleAcceptors = getPossibleAcceptorConnections();

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(connectionMapContainsSide(possibleTransmitters, side))
			{
				TileEntity tileEntity = Coord4D.get(tile()).getFromSide(side).getTileEntity(world());

				if(TransmissionType.checkTransmissionType(tileEntity, getTransmissionType()))
				{
					((DynamicNetwork<?,N>)getTransmitterNetwork()).merge(((IGridTransmitter<N>)tileEntity).getTransmitterNetwork());
				}
			}
		}

		((DynamicNetwork<?,N>)getTransmitterNetwork()).refresh(this);
		((DynamicNetwork<?,N>)getTransmitterNetwork()).refresh();
	}

	@Override
	public void onRefresh()
	{
		refreshTransmitterNetwork();
	}

	@Override
	public void onRedstoneSplit()
	{
		getTransmitterNetwork().split((IGridTransmitter<N>)tile());
		setTransmitterNetwork(null);
	}

	@Override
	public void setTransmitterNetwork(N network)
	{
		if(network != theNetwork)
		{
			removeFromTransmitterNetwork();
			theNetwork = network;
		}
	}

	@Override
	public boolean areTransmitterNetworksEqual(TileEntity tileEntity)
	{
		return tileEntity instanceof IGridTransmitter && getTransmissionType() == ((IGridTransmitter)tileEntity).getTransmissionType();
	}

	@Override
	public N getTransmitterNetwork()
	{
		return getTransmitterNetwork(true);
	}

	@Override
	public N getTransmitterNetwork(boolean createIfNull)
	{
		if(theNetwork == null && createIfNull)
		{
			byte possibleTransmitters = getPossibleTransmitterConnections();
			HashSet<N> connectedNets = new HashSet<N>();

			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if(connectionMapContainsSide(possibleTransmitters, side))
				{
					TileEntity cable = Coord4D.get(tile()).getFromSide(side).getTileEntity(world());

					if(TransmissionType.checkTransmissionType(cable, getTransmissionType()) && ((IGridTransmitter<N>)cable).getTransmitterNetwork(false) != null)
					{
						connectedNets.add(((IGridTransmitter<N>)cable).getTransmitterNetwork());
					}
				}
			}

			if(connectedNets.size() == 0)
			{
				theNetwork = createNetworkFromSingleTransmitter((IGridTransmitter<N>)tile());
			}
			else if(connectedNets.size() == 1)
			{
				N network = connectedNets.iterator().next();
				preSingleMerge(network);
				theNetwork = network;
				theNetwork.transmitters.add((IGridTransmitter<N>)tile());
				theNetwork.refresh();
				theNetwork.updateCapacity();
			}
			else {
				theNetwork = createNetworkByMergingSet(connectedNets);
				theNetwork.transmitters.add((IGridTransmitter<N>)tile());
			}
		}

		return theNetwork;
	}

	public void preSingleMerge(N network) {}

	@Override
	public void removeFromTransmitterNetwork()
	{
		if(theNetwork != null)
		{
			theNetwork.removeTransmitter((IGridTransmitter<N>)tile());
		}
	}

	@Override
	public void fixTransmitterNetwork()
	{
		getTransmitterNetwork().fixMessedUpNetwork((IGridTransmitter<N>) tile());
	}

	public abstract N createNetworkFromSingleTransmitter(IGridTransmitter<N> transmitter);

	public abstract N createNetworkByMergingSet(Set<N> networks);

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();

		getTransmitterNetwork().split(this);

		if(!world().isRemote)
		{
			TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
		}
		else {
			try {
				ClientTickHandler.killDeadNetworks();
			} catch(Exception e) {}
		}
	}

	@Override
	public void preRemove()
	{
		if(tile() instanceof IGridTransmitter)
		{
			getTransmitterNetwork().split((IGridTransmitter<N>)tile());

			if(!world().isRemote)
			{
				TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
			}
			else {
				try {
					ClientTickHandler.killDeadNetworks();
				} catch(Exception e) {}
			}
		}

		super.preRemove();
	}

	@Override
	protected void onModeChange(ForgeDirection side)
	{
		super.onModeChange(side);

		if(!world().isRemote)
		{
			Mekanism.packetHandler.sendToDimension(new TransmitterUpdateMessage(PacketType.UPDATE, Coord4D.get(tile())), world().provider.dimensionId);
		}
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();

		if(!world().isRemote)
		{
			Mekanism.packetHandler.sendToDimension(new TransmitterUpdateMessage(PacketType.UPDATE, Coord4D.get(tile())), world().provider.dimensionId);
		}
	}
	
	@Override
	public Coord4D getLocation()
	{
		return Coord4D.get(tile());
	}
	
	@Override
	public TileEntity getTile()
	{
		return tile();
	}

	@Override
	public void chunkLoad() {}
}
