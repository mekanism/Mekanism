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

import mekanism.api.ITransmitterNetwork;
import mekanism.api.Object3D;
import mekanism.api.TransmitterNetworkRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class FluidNetwork implements ITransmitterNetwork
{
	public Set<IMechanicalPipe> pipes = new HashSet<IMechanicalPipe>();
	
	public Set<IFluidHandler> possibleAcceptors = new HashSet<IFluidHandler>();
	public Map<IFluidHandler, ForgeDirection> acceptorDirections = new HashMap<IFluidHandler, ForgeDirection>();
	
	public FluidNetwork(IMechanicalPipe... varPipes)
	{
		pipes.addAll(Arrays.asList(varPipes));
	}
	
	public int emit(FluidStack fluidToSend, boolean doTransfer, TileEntity emitter)
	{
		List availableAcceptors = Arrays.asList(getFluidAcceptors(fluidToSend).toArray());
		
		Collections.shuffle(availableAcceptors);
		
		int fluidSent = 0;
		
		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = fluidToSend.amount % divider;
			int sending = (fluidToSend.amount-remaining)/divider;
			
			for(Object obj : availableAcceptors)
			{
				if(obj instanceof IFluidHandler && obj != emitter)
				{
					IFluidHandler acceptor = (IFluidHandler)obj;
					int currentSending = sending;
					
					if(remaining > 0)
					{
						currentSending++;
						remaining--;
					}
					
					fluidSent += acceptor.fill(acceptorDirections.get(acceptor), new FluidStack(fluidToSend.fluidID, currentSending), doTransfer);
				}
			}
		}
		
		if(doTransfer && fluidSent > 0 && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			FluidStack sendStack = fluidToSend.copy();
			sendStack.amount = fluidSent;
			MinecraftForge.EVENT_BUS.post(new FluidTransferEvent(this, sendStack));
		}
		
		return fluidSent;
	}
	
	public Set<IFluidHandler> getFluidAcceptors(FluidStack fluidToSend)
	{
		Set<IFluidHandler> toReturn = new HashSet<IFluidHandler>();
		
		for(IFluidHandler acceptor : possibleAcceptors)
		{
			if(acceptor.canFill(acceptorDirections.get(acceptor).getOpposite(), fluidToSend.getFluid()))
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
			IFluidHandler[] acceptors = PipeUtils.getConnectedAcceptors((TileEntity)pipe);
		
			for(IFluidHandler acceptor : acceptors)
			{
				if(acceptor != null && !(acceptor instanceof IMechanicalPipe))
				{
					possibleAcceptors.add(acceptor);
					acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
				}
			}
		}
	}

	public void merge(FluidNetwork network)
	{
		if(network != null && network != this)
		{
			FluidNetwork newNetwork = new FluidNetwork();
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
								FluidNetwork newNetwork = new FluidNetwork();

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
		public Object3D start;
		
		public List<Object3D> iterated = new ArrayList<Object3D>();
		public List<Object3D> toIgnore = new ArrayList<Object3D>();
		
		public NetworkFinder(World world, Object3D target, Object3D... ignore)
		{
			worldObj = world;
			start = target;
			
			if(ignore != null)
			{
				toIgnore = Arrays.asList(ignore);
			}
		}
		
		public void loopThrough(Object3D location)
		{
			iterated.add(location);
			
			if(iterated.contains(start))
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
			
			return iterated.contains(start);
		}
	}
	
	public static class FluidTransferEvent extends Event
	{
		public final FluidNetwork fluidNetwork;
		
		public final FluidStack fluidSent;
		
		public FluidTransferEvent(FluidNetwork network, FluidStack fluid)
		{
			fluidNetwork = network;
			fluidSent = fluid;
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
	
	public void tick()
	{
	}
		
	@Override
	public String toString()
	{
		return "[FluidNetwork] " + pipes.size() + " pipes, " + possibleAcceptors.size() + " acceptors.";
	}

	@Override
	public int getSize()
	{
		return pipes.size();
	}
}
