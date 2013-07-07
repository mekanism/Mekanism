package mekanism.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.FMLCommonHandler;

public class GasNetwork 
{
	public Set<IPressurizedTube> tubes = new HashSet<IPressurizedTube>();
	
	public Set<IGasAcceptor> possibleAcceptors = new HashSet<IGasAcceptor>();
	public Map<IGasAcceptor, ForgeDirection> acceptorDirections = new HashMap<IGasAcceptor, ForgeDirection>();
	
	public GasNetwork(IPressurizedTube... varPipes)
	{
		tubes.addAll(Arrays.asList(varPipes));
	}
	
	public int emit(int gasToSend, EnumGas transferType, TileEntity emitter)
	{
		List availableAcceptors = Arrays.asList(getGasAcceptors(transferType).toArray());
		
		Collections.shuffle(availableAcceptors);
		
		int prevSending = gasToSend;
		
		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = gasToSend % divider;
			int sending = (gasToSend-remaining)/divider;
			
			for(Object obj : availableAcceptors)
			{
				if(obj instanceof IGasAcceptor && obj != emitter)
				{
					IGasAcceptor acceptor = (IGasAcceptor)obj;
					
					int currentSending = sending;
					
					if(remaining > 0)
					{
						currentSending++;
						remaining--;
					}
					
					gasToSend -= (currentSending - acceptor.transferGasToAcceptor(currentSending, transferType));
				}
			}
		}
		
		if(prevSending > gasToSend && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, transferType));
		}
		
		return gasToSend;
	}
	
	public Set<IGasAcceptor> getGasAcceptors(EnumGas transferType)
	{
		Set<IGasAcceptor> toReturn = new HashSet<IGasAcceptor>();
		
		for(IGasAcceptor acceptor : possibleAcceptors)
		{
			if(acceptor.canReceiveGas(acceptorDirections.get(acceptor).getOpposite(), transferType))
			{
				if(!(acceptor instanceof IGasStorage) || (acceptor instanceof IGasStorage && (((IGasStorage)acceptor).getMaxGas(transferType) - ((IGasStorage)acceptor).getGas(transferType)) > 0))
				{
					toReturn.add(acceptor);
				}
			}
		}
		
		return toReturn;
	}

	public void refresh()
	{
		Iterator it = tubes.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			IPressurizedTube conductor = (IPressurizedTube)it.next();

			if(conductor == null)
			{
				it.remove();
			}
			else if(((TileEntity)conductor).isInvalid())
			{
				it.remove();
			}
			else {
				conductor.setNetwork(this);
			}
		}
		
		for(IPressurizedTube pipe : tubes)
		{
			IGasAcceptor[] acceptors = GasTransmission.getConnectedAcceptors((TileEntity)pipe);
		
			for(IGasAcceptor acceptor : acceptors)
			{
				if(acceptor != null && !(acceptor instanceof IPressurizedTube))
				{
					possibleAcceptors.add(acceptor);
					acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
				}
			}
		}
	}

	public void merge(GasNetwork network)
	{
		if(network != null && network != this)
		{
			GasNetwork newNetwork = new GasNetwork();
			newNetwork.tubes.addAll(tubes);
			newNetwork.tubes.addAll(network.tubes);
			newNetwork.refresh();
		}
	}

	public void split(IPressurizedTube splitPoint)
	{
		if(splitPoint instanceof TileEntity)
		{
			tubes.remove(splitPoint);
			
			TileEntity[] connectedBlocks = new TileEntity[6];
			
			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity sideTile = Object3D.get((TileEntity)splitPoint).getFromSide(direction).getTileEntity(((TileEntity)splitPoint).worldObj);
				
				if(sideTile != null)
				{
					connectedBlocks[Arrays.asList(ForgeDirection.values()).indexOf(direction)] = sideTile;
				}
			}

			for(int countOne = 0; countOne < connectedBlocks.length; countOne++)
			{
				TileEntity connectedBlockA = connectedBlocks[countOne];

				if(connectedBlockA instanceof IPressurizedTube)
				{
					for(int countTwo = 0; countTwo < connectedBlocks.length; countTwo++)
					{
						TileEntity connectedBlockB = connectedBlocks[countTwo];

						if(connectedBlockA != connectedBlockB && connectedBlockB instanceof IPressurizedTube)
						{
							NetworkFinder finder = new NetworkFinder(((TileEntity)splitPoint).worldObj, Object3D.get(connectedBlockB), Object3D.get((TileEntity)splitPoint));

							if(finder.foundTarget(Object3D.get(connectedBlockA)))
							{
								for(Object3D node : finder.iterated)
								{
									TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).worldObj);

									if(nodeTile instanceof IPressurizedTube)
									{
										if(nodeTile != splitPoint)
										{
											((IPressurizedTube)nodeTile).setNetwork(this);
										}
									}
								}
							}
							else {
								GasNetwork newNetwork = new GasNetwork();

								for(Object3D node : finder.iterated)
								{
									TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).worldObj);

									if(nodeTile instanceof IPressurizedTube)
									{
										if(nodeTile != splitPoint)
										{
											newNetwork.tubes.add((IPressurizedTube)nodeTile);
										}
									}
								}

								newNetwork.refresh();
							}
						}
					}
				}
			}
		}
	}
	
	public static class NetworkFinder
	{
		public World worldObj;
		public Object3D toFind;
		
		public List<Object3D> iterated = new ArrayList<Object3D>();
		public List<Object3D> toIgnore = new ArrayList<Object3D>();
		
		public NetworkFinder(World world, Object3D target, Object3D... ignore)
		{
			worldObj = world;
			toFind = target;
			
			if(ignore != null)
			{
				toIgnore = Arrays.asList(ignore);
			}
		}
		
		public void loopThrough(Object3D location)
		{
			iterated.add(location);
			
			if(iterated.contains(toFind))
			{
				return;
			}
			
			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				Object3D obj = location.getFromSide(direction);
				
				if(!iterated.contains(obj) && !toIgnore.contains(obj))
				{
					TileEntity tileEntity = location.getTileEntity(worldObj);
					TileEntity sideTile = obj.getTileEntity(worldObj);
					
					if(sideTile instanceof IPressurizedTube && ((IPressurizedTube)sideTile).canTransferGas())
					{
						if(((IPressurizedTube)sideTile).canTransferGasToTube(tileEntity) && ((IPressurizedTube)tileEntity).canTransferGasToTube(sideTile))
						{
							loopThrough(obj);
						}
					}
				}
			}
		}
		
		public boolean foundTarget(Object3D start)
		{
			loopThrough(start);
			
			return iterated.contains(toFind);
		}
	}
	
	public static class GasTransferEvent extends Event
	{
		public final GasNetwork gasNetwork;
		
		public final EnumGas transferType;
		
		public GasTransferEvent(GasNetwork network, EnumGas type)
		{
			gasNetwork = network;
			transferType = type;
		}
	}
	
	public static class NetworkLoader
	{
		@ForgeSubscribe
		public void onChunkLoad(ChunkEvent.Load event)
		{
			if(event.getChunk() != null)
			{
				for(Object obj : event.getChunk().chunkTileEntityMap.values())
				{
					if(obj instanceof TileEntity)
					{
						TileEntity tileEntity = (TileEntity)obj;
						
						if(tileEntity instanceof IPressurizedTube)
						{
							((IPressurizedTube)tileEntity).refreshNetwork();
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "[GasNetwork] " + tubes.size() + " pipes, " + possibleAcceptors.size() + " acceptors.";
	}
}
