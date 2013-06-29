package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.common.FMLCommonHandler;

import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;

public class LiquidNetwork
{
	public Set<IMechanicalPipe> pipes = new HashSet<IMechanicalPipe>();
	
	public Set<ITankContainer> possibleAcceptors = new HashSet<ITankContainer>();
	public Map<ITankContainer, ForgeDirection> acceptorDirections = new HashMap<ITankContainer, ForgeDirection>();
	
	public LiquidNetwork(IMechanicalPipe... varPipes)
	{
		pipes.addAll(Arrays.asList(varPipes));
	}
	
	public int emit(LiquidStack liquidToSend, boolean doTransfer, TileEntity emitter)
	{
		List availableAcceptors = Arrays.asList(getLiquidAcceptors(liquidToSend).toArray());
		
		Collections.shuffle(availableAcceptors);
		
		int liquidSent = 0;
		
		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = liquidToSend.amount % divider;
			int sending = (liquidToSend.amount-remaining)/divider;
			
			for(Object obj : availableAcceptors)
			{
				if(obj instanceof ITankContainer && obj != emitter)
				{
					ITankContainer acceptor = (ITankContainer)obj;
					int currentSending = sending;
					
					if(remaining > 0)
					{
						currentSending++;
						remaining--;
					}
					
					liquidSent += acceptor.fill(acceptorDirections.get(acceptor), new LiquidStack(liquidToSend.itemID, currentSending, liquidToSend.itemMeta), doTransfer);
				}
			}
		}
		
		if(doTransfer && liquidSent > 0 && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			LiquidStack sendStack = liquidToSend.copy();
			sendStack.amount = liquidSent;
			MinecraftForge.EVENT_BUS.post(new LiquidTransferEvent(this, sendStack));
		}
		
		return liquidSent;
	}
	
	public Set<ITankContainer> getLiquidAcceptors(LiquidStack liquidToSend)
	{
		Set<ITankContainer> toReturn = new HashSet<ITankContainer>();
		
		for(ITankContainer acceptor : possibleAcceptors)
		{
			if(acceptor.fill(acceptorDirections.get(acceptor).getOpposite(), liquidToSend, false) > 0)
			{
				toReturn.add(acceptor);
			}
		}
		
		return toReturn;
	}

	public void refresh()
	{
		Iterator it = pipes.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			IMechanicalPipe conductor = (IMechanicalPipe)it.next();

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
		
		for(IMechanicalPipe pipe : pipes)
		{
			ITankContainer[] acceptors = PipeUtils.getConnectedAcceptors((TileEntity)pipe);
		
			for(ITankContainer acceptor : acceptors)
			{
				if(acceptor != null && !(acceptor instanceof IMechanicalPipe))
				{
					possibleAcceptors.add(acceptor);
					acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
				}
			}
		}
	}

	public void merge(LiquidNetwork network)
	{
		if(network != null && network != this)
		{
			LiquidNetwork newNetwork = new LiquidNetwork();
			newNetwork.pipes.addAll(pipes);
			newNetwork.pipes.addAll(network.pipes);
			newNetwork.refresh();
		}
	}

	public void split(IMechanicalPipe splitPoint)
	{
		if(splitPoint instanceof TileEntity)
		{
			pipes.remove(splitPoint);
			
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

				if(connectedBlockA instanceof IMechanicalPipe)
				{
					for(int countTwo = 0; countTwo < connectedBlocks.length; countTwo++)
					{
						TileEntity connectedBlockB = connectedBlocks[countTwo];

						if(connectedBlockA != connectedBlockB && connectedBlockB instanceof IMechanicalPipe)
						{
							NetworkFinder finder = new NetworkFinder(((TileEntity)splitPoint).worldObj, Object3D.get(connectedBlockB), Object3D.get((TileEntity)splitPoint));

							if(finder.foundTarget(Object3D.get(connectedBlockA)))
							{
								for(Object3D node : finder.iterated)
								{
									TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).worldObj);

									if(nodeTile instanceof IMechanicalPipe)
									{
										if(nodeTile != splitPoint)
										{
											((IMechanicalPipe)nodeTile).setNetwork(this);
										}
									}
								}
							}
							else {
								LiquidNetwork newNetwork = new LiquidNetwork();

								for(Object3D node : finder.iterated)
								{
									TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).worldObj);

									if(nodeTile instanceof IMechanicalPipe)
									{
										if(nodeTile != splitPoint)
										{
											newNetwork.pipes.add((IMechanicalPipe)nodeTile);
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
					TileEntity tileEntity = obj.getTileEntity(worldObj);
					
					if(tileEntity instanceof IMechanicalPipe)
					{
						loopThrough(obj);
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
	
	public static class LiquidTransferEvent extends Event
	{
		public final LiquidNetwork liquidNetwork;
		
		public final LiquidStack liquidSent;
		
		public LiquidTransferEvent(LiquidNetwork network, LiquidStack liquid)
		{
			liquidNetwork = network;
			liquidSent = liquid;
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
						
						if(tileEntity instanceof IMechanicalPipe)
						{
							((IMechanicalPipe)tileEntity).refreshNetwork();
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "[LiquidNetwork] " + pipes.size() + " pipes, " + possibleAcceptors.size() + " acceptors.";
	}
}
