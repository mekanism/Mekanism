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
import mekanism.api.transmitters.ITransmitterTile;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class PartTransmitter<A, N extends DynamicNetwork<A, N>> extends PartSidedPipe implements ITransmitterTile<A, N>, IAlloyInteraction
{
	public MultipartTransmitter<A, N> transmitterDelegate;

	public boolean unloaded = true;

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

		unloaded = false;
	}

	public abstract N createNewNetwork();

	public abstract N createNetworkByMerging(Collection<N> networks);

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();

		unloaded = true;
		
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
						Coord4D thisCoord = Coord4D.get(tile());
						
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
}
