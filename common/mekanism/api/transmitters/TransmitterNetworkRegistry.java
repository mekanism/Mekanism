package mekanism.api.transmitters;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class TransmitterNetworkRegistry implements ITickHandler
{
	private static TransmitterNetworkRegistry INSTANCE = new TransmitterNetworkRegistry();
	private static boolean loaderRegistered = false;

	private HashSet<ITransmitterNetwork> networks = new HashSet<ITransmitterNetwork>();

	public TransmitterNetworkRegistry()
	{
		TickRegistry.registerTickHandler(this, Side.SERVER);
	}

	public static void initiate()
	{
		if(!loaderRegistered)
		{
			loaderRegistered = true;

			MinecraftForge.EVENT_BUS.register(new NetworkLoader());
		}
	}

	public static TransmitterNetworkRegistry getInstance()
	{
		return INSTANCE;
	}

	public void registerNetwork(ITransmitterNetwork network)
	{
		networks.add(network);
	}

	public void removeNetwork(ITransmitterNetwork network)
	{
		if(networks.contains(network))
		{
			networks.remove(network);
		}
	}

	public void pruneEmptyNetworks()
	{
	    HashSet<ITransmitterNetwork> copySet = new HashSet<ITransmitterNetwork>(networks);
	    
		for(ITransmitterNetwork e : copySet)
		{
			if(e.getSize() == 0)
			{
				removeNetwork(e);
			}
		}
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		Set<ITransmitterNetwork> iterNetworks = (Set<ITransmitterNetwork>)networks.clone();

		for(ITransmitterNetwork net : iterNetworks)
		{
			if(networks.contains(net))
			{
				net.tick();
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public String getLabel()
	{
		return "MekanismNetworks";
	}

	@Override
	public String toString()
	{
		return "Network Registry:\n" + networks;
	}

	public String[] toStrings()
	{
		String[] strings = new String[networks.size()];
		int i = 0;
		
		for(ITransmitterNetwork<?, ?> network : networks)
		{
			strings[i] = network.toString();
			++i;
		}
		
		return strings;
	}

	public static class NetworkLoader
	{
		@ForgeSubscribe
		public void onChunkLoad(ChunkEvent.Load event)
		{
			if(event.getChunk() != null && !event.world.isRemote)
			{
				int x = event.getChunk().xPosition;
				int z = event.getChunk().zPosition;

				IChunkProvider cProvider = event.getChunk().worldObj.getChunkProvider();
				Chunk[] neighbors = new Chunk[5];

				neighbors[0] = event.getChunk();

				if(cProvider.chunkExists(x + 1, z)) neighbors[1] = cProvider.provideChunk(x + 1, z);
				if(cProvider.chunkExists(x - 1, z)) neighbors[2] = cProvider.provideChunk(x - 1, z);
				if(cProvider.chunkExists(x, z + 1)) neighbors[3] = cProvider.provideChunk(x, z + 1);
				if(cProvider.chunkExists(x, z - 1)) neighbors[4] = cProvider.provideChunk(x, z - 1);

				for(Chunk c : neighbors)
				{
					refreshChunk(c);
				}
			}
		}

		public synchronized void refreshChunk(Chunk c)
		{
			try {
			    if(c != null)
	            {
			    	Map copy = (Map)((HashMap)c.chunkTileEntityMap).clone();
			    	
	                for(Iterator iter = copy.values().iterator(); iter.hasNext();)
	                {
	                	Object obj = iter.next();
	                	
	                    if(obj instanceof IGridTransmitter)
	                    {
                            ((IGridTransmitter)obj).refreshTransmitterNetwork();
                            ((IGridTransmitter)obj).chunkLoad();
	                    }
	                }
	            }
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
