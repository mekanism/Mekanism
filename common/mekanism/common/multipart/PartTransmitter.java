package mekanism.common.multipart;

import java.util.HashSet;
import java.util.Set;

import codechicken.multipart.*;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.client.ClientTickHandler;
import mekanism.common.IConfigurable;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTransmitterUpdate;
import mekanism.common.network.PacketTransmitterUpdate.PacketType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public abstract class PartTransmitter<N extends DynamicNetwork<?, N>> extends PartSidedPipe implements ITransmitter<N>, IConfigurable
{
	public N theNetwork;
	
	@Override
	public void bind(TileMultipart t)
	{
		if(tile() != null && theNetwork != null)
		{
			getTransmitterNetwork().transmitters.remove(tile());
			super.bind(t);
			getTransmitterNetwork().transmitters.add((ITransmitter<N>)tile());
		}
		else {
			super.bind(t);
		}
	}
	
	@Override
	public void update()
	{
        if(world().isRemote)
        {
            if(delayTicks == 5)
            {
                delayTicks = 6; /* don't refresh again */
                refreshTransmitterNetwork();
            }
            else if(delayTicks < 5)
            {
                delayTicks++;
            }
        }

        if(sendDesc)
		{
			sendDescUpdate();
			sendDesc = false;
		}
	}

	public static boolean connectionMapContainsSide(byte connections, ForgeDirection side)
	{
		byte tester = (byte)(1 << side.ordinal());
		return (connections & tester) > 0;
	}
	
	@Override
	public void refreshTransmitterNetwork()
	{
		byte possibleTransmitters = getPossibleTransmitterConnections();
		byte possibleAcceptors = getPossibleAcceptorConnections();
		
		if(possibleTransmitters != currentTransmitterConnections)
		{
			boolean nowPowered = world().isBlockIndirectlyGettingPowered(x(), y(), z());
			
			if(nowPowered != redstonePowered)
			{
				if(nowPowered)
				{
					getTransmitterNetwork().split((ITransmitter<N>)tile());
					setTransmitterNetwork(null);
				}
				
				tile().notifyPartChange(this);
				
				redstonePowered = nowPowered;
			}
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if(connectionMapContainsSide(possibleTransmitters, side))
				{
					TileEntity tileEntity = Coord4D.get(tile()).getFromSide(side).getTileEntity(world());
					
					if(TransmissionType.checkTransmissionType(tileEntity, getTransmissionType()) && isConnectable(tileEntity))
					{
						((DynamicNetwork<?,N>)getTransmitterNetwork()).merge(((ITransmitter<N>)tileEntity).getTransmitterNetwork());
					}
				}
			}
		}

		((DynamicNetwork<?,N>)getTransmitterNetwork()).refresh();
		
		if(!world().isRemote)
		{
			currentTransmitterConnections = possibleTransmitters;
			currentAcceptorConnections = possibleAcceptors;
			
			sendDesc = true;
		}
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
		return tileEntity instanceof ITransmitter && getTransmissionType() == ((ITransmitter)tileEntity).getTransmissionType();
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
					
					if(TransmissionType.checkTransmissionType(cable, getTransmissionType()) && ((ITransmitter<N>)cable).getTransmitterNetwork(false) != null)
					{
						connectedNets.add(((ITransmitter<N>)cable).getTransmitterNetwork());
					}
				}
			}
			
			if(connectedNets.size() == 0)
			{
				theNetwork = createNetworkFromSingleTransmitter((ITransmitter<N>)tile());
			}
			else if(connectedNets.size() == 1)
			{
				N network = connectedNets.iterator().next();
				preSingleMerge(network);
				theNetwork = network;
				theNetwork.transmitters.add((ITransmitter<N>)tile());
				theNetwork.refresh();
			}
			else {
				theNetwork = createNetworkByMergingSet(connectedNets);
				theNetwork.transmitters.add((ITransmitter<N>)tile());
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
			theNetwork.removeTransmitter((ITransmitter<N>)tile());
		}
	}

	@Override
	public void fixTransmitterNetwork()
	{
		getTransmitterNetwork().fixMessedUpNetwork((ITransmitter<N>) tile());
	}
	
	public abstract N createNetworkFromSingleTransmitter(ITransmitter<N> transmitter);
	
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
		if(tile() instanceof ITransmitter)
		{
			getTransmitterNetwork().split((ITransmitter<N>)tile());
			
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
	public void onModeChange(ForgeDirection side)
	{
		refreshTransmitterNetwork();
		
		if(!world().isRemote)
		{
			PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketTransmitterUpdate().setParams(PacketType.UPDATE, tile()), world().provider.dimensionId);
		}
	}
	
	@Override
	public void onAdded()
	{
		super.onAdded();
		refreshTransmitterNetwork();
	}

	@Override
	public void onChunkLoad()
	{
		super.onChunkLoad();
		refreshTransmitterNetwork();
	}
	
	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();
		
		if(!world().isRemote)
		{
			PacketHandler.sendPacket(Transmission.CLIENTS_DIM, new PacketTransmitterUpdate().setParams(PacketType.UPDATE, tile()), world().provider.dimensionId);
		}
		
		refreshTransmitterNetwork();
	}
	
	@Override
	public void onPartChanged(TMultiPart part)
	{
		super.onPartChanged(part);
		refreshTransmitterNetwork();
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		fixTransmitterNetwork();
		return true;
	}

	@Override
	public void chunkLoad() {}
	
	@Override
	public boolean canConnectToAcceptor(ForgeDirection side)
	{
		if(!isValidAcceptor(Coord4D.get(tile()).getFromSide(side).getTileEntity(world()), side))
		{
			return false;
		}
		
		return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PUSH;
	}
}
