package mekanism.api.transmitters;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
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
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		return;
	}
	
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
	
	public static class NetworkLoader
	{
		@ForgeSubscribe
		public void onChunkLoad(ChunkEvent.Load event)
		{
			if(event.getChunk() != null)
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
		
		public void refreshChunk(Chunk c)
		{
			if(c != null)
			{
				for(Object obj : c.chunkTileEntityMap.values())
				{
					if(obj instanceof TileEntity)
					{
						TileEntity tileEntity = (TileEntity)obj;
						
						if(tileEntity instanceof ITransmitter)
						{
							((ITransmitter)tileEntity).refreshTransmitterNetwork();
						}
					}
				}
			}
		}
	}
}
