package mekanism.common.multipart;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;

import mekanism.api.Coord4D;
import mekanism.api.IAlloyInteraction;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.DynamicNetwork.NetworkClientRequest;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;

public abstract class PartTransmitter<A, N extends DynamicNetwork<A, N>> extends PartSidedPipe implements IAlloyInteraction
{
	public MultipartTransmitter<A, N> transmitterDelegate;

	public boolean unloaded = true;
	
	public boolean dataRequest = false;

	public PartTransmitter()
	{
		transmitterDelegate = new MultipartTransmitter<>(this);
	}

	public MultipartTransmitter<A, N> getTransmitter()
	{
		return transmitterDelegate;
	}

	public abstract N createNewNetwork();

	public abstract N createNetworkByMerging(Collection<N> networks);

	@Override
	public void onWorldJoin()
	{
		if(!getWorld().isRemote)
		{
			TransmitterNetworkRegistry.registerOrphanTransmitter(getTransmitter());
		}

		unloaded = false;
	}
	
	@Override
	public void update()
	{
		super.update();
		
		if(getWorld().isRemote)
		{
			if(!dataRequest)
			{
				dataRequest = true;
				MinecraftForge.EVENT_BUS.post(new NetworkClientRequest(getWorld().getTileEntity(getPos())));
			}
		}
	}
	
	@Override
	public void onWorldSeparate() {}

	@Override
	public void onUnloaded()
	{
		super.onUnloaded();

		unloaded = true;
		
		if(!getWorld().isRemote)
		{
			getTransmitter().takeShare();
			TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
		}
		else {
			getTransmitter().setTransmitterNetwork(null);
		}
	}

	@Override
	public void onRemoved()
	{
		if(!getWorld().isRemote)
		{
			TransmitterNetworkRegistry.invalidateTransmitter(getTransmitter());
		} 
		else {
			getTransmitter().setTransmitterNetwork(null);
		}

		super.onRemoved();
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
	public void markDirtyAcceptor(EnumFacing side)
	{
		super.markDirtyAcceptor(side);
		
		if(getTransmitter().hasTransmitterNetwork())
		{
			getTransmitter().getTransmitterNetwork().acceptorChanged(getTransmitter(), side);
		}
	}

	public A getCachedAcceptor(EnumFacing side)
	{
		ConnectionType type = connectionTypes[side.ordinal()];
		
		if(type == ConnectionType.PULL || type == ConnectionType.NONE)
		{
			return null;
		}
		
		return connectionMapContainsSide(currentAcceptorConnections, side) ? (A)cachedAcceptors[side.ordinal()] : null;
	}
	
	@Override
	public void onAlloyInteraction(EntityPlayer player, int tierOrdinal) 
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			int upgraded = 0;
			Object[] array = ((LinkedHashSet)getTransmitter().getTransmitterNetwork().transmitters.clone()).toArray();
			
			Arrays.sort(array, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) 
				{
					if(o1 instanceof IGridTransmitter && o2 instanceof IGridTransmitter)
					{
						Coord4D thisCoord = new Coord4D(getPos(), getWorld());
						
						Coord4D o1Coord = ((IGridTransmitter)o1).coord();
						Coord4D o2Coord = ((IGridTransmitter)o2).coord();
						
						return o1Coord.distanceTo(thisCoord) > o2Coord.distanceTo(thisCoord) ? 1 : 
							(o1Coord.distanceTo(thisCoord) < o2Coord.distanceTo(thisCoord) ? -1 : 0);
					}
					
					return 0;
				}
			});
			
			for(Object iter : array)
			{
				if(iter instanceof MultipartTransmitter)
				{
					PartTransmitter t = ((MultipartTransmitter)iter).containingPart;
					
					if(t.upgrade(tierOrdinal))
					{
						upgraded++;
						
						if(upgraded == 8)
						{
							break;
						}
					}
				}
			}
			
			if(upgraded > 0)
			{
				if(!player.capabilities.isCreativeMode)
				{
					player.getCurrentEquippedItem().stackSize--;
					
					if(player.getCurrentEquippedItem().stackSize == 0)
					{
						player.setCurrentItemOrArmor(0, null);
					}
				}
			}
		}
	}
	
	public boolean upgrade(int tierOrdinal)
	{
		return false;
	}

	public abstract int getCapacity();

	public abstract Object getBuffer();

	public abstract void takeShare();

    public abstract void updateShare();

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == Capabilities.GRID_TRANSMITTER_CAPABILITY || super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == Capabilities.GRID_TRANSMITTER_CAPABILITY)
		{
			return (T)getTransmitter();
		}
		
		return super.getCapability(capability, side);
	}
}
