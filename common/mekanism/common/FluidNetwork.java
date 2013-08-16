package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import mekanism.api.DynamicNetwork;
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

public class FluidNetwork extends DynamicNetwork implements ITransmitterNetwork
{
	public HashSet<IMechanicalPipe> pipes = new HashSet<IMechanicalPipe>();
	
	public Set<IFluidHandler> possibleAcceptors = new HashSet<IFluidHandler>();
	public Map<IFluidHandler, ForgeDirection> acceptorDirections = new HashMap<IFluidHandler, ForgeDirection>();
	
	public FluidNetwork(IMechanicalPipe... varPipes)
	{
		pipes.addAll(Arrays.asList(varPipes));
		register();
	}
	
	public FluidNetwork(Set<FluidNetwork> networks)
	{
		for(FluidNetwork net : networks)
		{
			if(net != null)
			{
				addAllPipes(net.pipes);
				net.deregister();
			}
		}
		
		refresh();
		register();
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
		Set<IMechanicalPipe> iterPipes = (Set<IMechanicalPipe>) pipes.clone();
		Iterator it = iterPipes.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			IMechanicalPipe conductor = (IMechanicalPipe)it.next();

			if(conductor == null || ((TileEntity)conductor).isInvalid())
			{
				it.remove();
				pipes.remove(conductor);
			}
			else {
				conductor.setNetwork(this);
			}
		}
		
		for(IMechanicalPipe pipe : iterPipes)
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
			Set<FluidNetwork> networks = new HashSet<FluidNetwork>();
			networks.add(this);
			networks.add(network);
			FluidNetwork newNetwork = new FluidNetwork(networks);
			newNetwork.refresh();
		}
	}
	
	public void addAllPipes(Set<IMechanicalPipe> newPipes)
	{
		pipes.addAll(newPipes);
	}

	public void split(IMechanicalPipe splitPoint)
	{
		if(splitPoint instanceof TileEntity)
		{
			removePipe(splitPoint);
			
			TileEntity[] connectedBlocks = new TileEntity[6];
			boolean[] dealtWith = {false, false, false, false, false, false};
			
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

				if(connectedBlockA instanceof IMechanicalPipe && !dealtWith[countOne])
				{
					NetworkFinder finder = new NetworkFinder(((TileEntity)splitPoint).worldObj, Object3D.get(connectedBlockA), Object3D.get((TileEntity)splitPoint));
					List<Object3D> partNetwork = finder.exploreNetwork();
					
					for(int countTwo = countOne + 1; countTwo < connectedBlocks.length; countTwo++)
					{
						TileEntity connectedBlockB = connectedBlocks[countTwo];
						
						if(connectedBlockB instanceof IMechanicalPipe && !dealtWith[countTwo])
						{
							if(partNetwork.contains(Object3D.get(connectedBlockB)))
							{
								dealtWith[countTwo] = true;
							}
						}
					}
					
					Set<IMechanicalPipe> newNetPipes= new HashSet<IMechanicalPipe>();
					for(Object3D node : finder.iterated)
					{
						TileEntity nodeTile = node.getTileEntity(((TileEntity)splitPoint).worldObj);

						if(nodeTile instanceof IMechanicalPipe)
						{
							if(nodeTile != splitPoint)
							{
								newNetPipes.add((IMechanicalPipe)nodeTile);
							}
						}
					}
					
					FluidNetwork newNetwork = new FluidNetwork(newNetPipes.toArray(new IMechanicalPipe[0]));					
					newNetwork.refresh();
				}
			}
			
			deregister();
		}
	}
	
	public void fixMessedUpNetwork(IMechanicalPipe pipe)
	{
		if(pipe instanceof TileEntity)
		{
			NetworkFinder finder = new NetworkFinder(((TileEntity)pipe).getWorldObj(), Object3D.get((TileEntity)pipe), null);
			List<Object3D> partNetwork = finder.exploreNetwork();
			Set<IMechanicalPipe> newPipes = new HashSet<IMechanicalPipe>();
			
			for(Object3D node : partNetwork)
			{
				TileEntity nodeTile = node.getTileEntity(((TileEntity)pipe).worldObj);

				if(nodeTile instanceof IMechanicalPipe)
				{
					((IMechanicalPipe) nodeTile).removeFromNetwork();
					newPipes.add((IMechanicalPipe)nodeTile);
				}
			}
			
			FluidNetwork newNetwork = new FluidNetwork(newPipes.toArray(new IMechanicalPipe[0]));
			newNetwork.refresh();
			newNetwork.fixed = true;
			deregister();
		}
	}
	
	public void removePipe(IMechanicalPipe pipe)
	{
		pipes.remove(pipe);
		if(pipes.size() == 0)
		{
			deregister();
		}
	}
	
	public void register()
	{
		try {
			IMechanicalPipe aPipe = pipes.iterator().next();
			
			if(aPipe instanceof TileEntity && !((TileEntity)aPipe).worldObj.isRemote)
			{
				TransmitterNetworkRegistry.getInstance().registerNetwork(this);			
			}
		} catch(NoSuchElementException e) {}
	}
	
	public void deregister()
	{
		pipes.clear();
		TransmitterNetworkRegistry.getInstance().removeNetwork(this);
	}
	
	public static class NetworkFinder
	{
		public World worldObj;
		public Object3D start;
		
		public List<Object3D> iterated = new ArrayList<Object3D>();
		public List<Object3D> toIgnore = new ArrayList<Object3D>();
		
		public NetworkFinder(World world, Object3D location, Object3D... ignore)
		{
			worldObj = world;
			start = location;
			
			if(ignore != null)
			{
				toIgnore = Arrays.asList(ignore);
			}
		}

		public void loopAll(Object3D location)
		{
			if(location.getTileEntity(worldObj) instanceof IMechanicalPipe)
			{
				iterated.add(location);
			}
			
			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				Object3D obj = location.getFromSide(direction);
				
				if(!iterated.contains(obj) && !toIgnore.contains(obj))
				{
					TileEntity tileEntity = obj.getTileEntity(worldObj);
					
					if(tileEntity instanceof IMechanicalPipe)
					{
						loopAll(obj);
					}
				}
			}
		}

		public List<Object3D> exploreNetwork()
		{
			loopAll(start);
			
			return iterated;
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
		//Fix weird behaviour periodically.
		if(!fixed)
		{
			++ticksSinceCreate;
			if(ticksSinceCreate > 1200)
			{
				ticksSinceCreate = 0;
				fixMessedUpNetwork(pipes.iterator().next());
			}
		}
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
