package mekanism.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mekanism.api.ListUtils;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.ITransmitterNetwork;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.util.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class FluidNetwork extends DynamicNetwork<IFluidHandler, FluidNetwork>
{
	public int transferDelay = 0;
	
	public boolean didTransfer;
	public boolean prevTransfer;
	
	public float fluidScale;
	
	public Fluid refFluid;
	
	public FluidStack fluidStored;
	public int prevStored;

	public int prevTransferAmount = 0;
	
	public FluidNetwork(IGridTransmitter<FluidNetwork>... varPipes)
	{
		transmitters.addAll(Arrays.asList(varPipes));
		register();
	}
	
	public FluidNetwork(Collection<IGridTransmitter<FluidNetwork>> collection)
	{
		transmitters.addAll(collection);
		register();
	}
	
	public FluidNetwork(Set<FluidNetwork> networks)
	{
		for(FluidNetwork net : networks)
		{
			if(net != null)
			{
				if(FMLCommonHandler.instance().getEffectiveSide().isClient())
				{
					if(net.refFluid != null && net.fluidScale > fluidScale)
					{
						refFluid = net.refFluid;
						fluidScale = net.fluidScale;
						fluidStored = net.fluidStored;
						
						net.fluidScale = 0;
						net.refFluid = null;
						net.fluidStored = null;
					}
				}
				else {
					if(net.fluidStored != null)
					{
						if(fluidStored == null)
						{
							fluidStored = net.fluidStored;
						}
						else {
							fluidStored.amount += net.fluidStored.amount;
						}
						
						net.fluidStored = null;
					}
				}
				
				addAllTransmitters(net.transmitters);
				net.deregister();
			}
		}
		
		fluidScale = getScale();
		
		refresh();
		register();
	}
	
    @Override
    public void onNetworksCreated(List<FluidNetwork> networks)
    {    	
		if(fluidStored != null && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
	    	int[] caps = new int[networks.size()];
	    	int cap = 0;
	    	
	    	for(FluidNetwork network : networks)
	    	{
	    		caps[networks.indexOf(network)] = network.getCapacity();
	    		cap += network.getCapacity();
	    	}
	    	
	    	fluidStored.amount = Math.min(cap, fluidStored.amount);
	    	
	    	int[] values = ListUtils.calcPercentInt(ListUtils.percent(caps), fluidStored.amount);
	    	
	    	for(FluidNetwork network : networks)
	    	{
	    		int index = networks.indexOf(network);
	    		
	    		if(values[index] > 0)
	    		{
	    			network.fluidStored = new FluidStack(fluidStored.getFluid(), values[index]);
	    			network.fluidScale = network.getScale();
	    			network.refFluid = fluidStored.getFluid();
	    		}
	    	}
		}
		
    	fluidScale = 0;
	   	fluidStored = null;
    	refFluid = null;
    }
	
	public synchronized int getFluidNeeded()
	{
		return getCapacity()-(fluidStored != null ? fluidStored.amount : 0);
	}
	
	public synchronized int tickEmit(FluidStack fluidToSend, boolean doTransfer)
	{
		List availableAcceptors = Arrays.asList(getAcceptors(fluidToSend).toArray());
		
		Collections.shuffle(availableAcceptors);
		
		int fluidSent = 0;
		
		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = fluidToSend.amount % divider;
			int sending = (fluidToSend.amount-remaining)/divider;
			
			for(Object obj : availableAcceptors)
			{
				if(obj instanceof IFluidHandler)
				{
					IFluidHandler acceptor = (IFluidHandler)obj;
					int currentSending = sending;
					
					if(remaining > 0)
					{
						currentSending++;
						remaining--;
					}
					
					fluidSent += acceptor.fill(acceptorDirections.get(acceptor).getOpposite(), new FluidStack(fluidToSend.fluidID, currentSending), doTransfer);
				}
			}
		}
		
		if(doTransfer && fluidSent > 0 && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			didTransfer = true;
			transferDelay = 2;
		}
		
		return fluidSent;
	}
	
	public synchronized int emit(FluidStack fluidToSend, boolean doTransfer)
	{
		if(fluidToSend == null || (fluidStored != null && fluidStored.getFluid() != fluidToSend.getFluid()))
		{
			return 0;
		}
		
		int toUse = Math.min(getFluidNeeded(), fluidToSend.amount);
		
		if(doTransfer)
		{
			if(fluidStored == null)
			{
				fluidStored = fluidToSend.copy();
				fluidStored.amount = toUse;
			}
			else {
				fluidStored.amount += toUse;
			}
		}
		
		return toUse;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			if(transferDelay == 0)
			{
				didTransfer = false;
			}
			else {
				transferDelay--;
			}
			
			int stored = fluidStored != null ? fluidStored.amount : 0;
			
			if(stored != prevStored)
			{
				needsUpdate = true;
			}
			
			prevStored = stored;
			
			if(didTransfer != prevTransfer || needsUpdate)
			{
				MinecraftForge.EVENT_BUS.post(new FluidTransferEvent(this, fluidStored, didTransfer));
				needsUpdate = false;
			}
			
			prevTransfer = didTransfer;
			
			if(fluidStored != null)
			{
				fluidStored.amount -= tickEmit(fluidStored, true);
				
				if(fluidStored.amount <= 0)
				{
					fluidStored = null;
				}
			}
		}
	}
	
	@Override
	public void clientTick()
	{
		super.clientTick();
		
		fluidScale = Math.max(fluidScale, getScale());
		
		if(didTransfer && fluidScale < 1)
		{
			fluidScale = Math.max(getScale(), Math.min(1, fluidScale+0.02F));
		}
		else if(!didTransfer && fluidScale > 0)
		{
			fluidScale = getScale();
			
			if(fluidScale == 0)
			{
				fluidStored = null;
			}
		}
	}
	
	@Override
	public synchronized Set<IFluidHandler> getAcceptors(Object... data)
	{
		FluidStack fluidToSend = (FluidStack)data[0];
		Set<IFluidHandler> toReturn = new HashSet<IFluidHandler>();
		
		for(IFluidHandler acceptor : possibleAcceptors)
		{
			if(acceptorDirections.get(acceptor) == null)
			{
				continue;
			}
			
			if(acceptor.canFill(acceptorDirections.get(acceptor).getOpposite(), fluidToSend.getFluid()))
			{
				toReturn.add(acceptor);
			}
		}
		
		return toReturn;
	}
 
	@Override
	public synchronized void refresh()
	{
		Set<IGridTransmitter<FluidNetwork>> iterPipes = (Set<IGridTransmitter<FluidNetwork>>)transmitters.clone();
		Iterator it = iterPipes.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			IGridTransmitter<FluidNetwork> conductor = (IGridTransmitter<FluidNetwork>)it.next();

			if(conductor == null || ((TileEntity)conductor).isInvalid())
			{
				it.remove();
				transmitters.remove(conductor);
			}
			else {
				conductor.setTransmitterNetwork(this);
			}
		}
		
		for(IGridTransmitter<FluidNetwork> transmitter : iterPipes)
		{
			IFluidHandler[] acceptors = PipeUtils.getConnectedAcceptors((TileEntity)transmitter);
		
			for(IFluidHandler acceptor : acceptors)
			{
				ForgeDirection side = ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor));
				
				if(side != null && acceptor != null && !(acceptor instanceof IGridTransmitter) && transmitter.canConnectToAcceptor(side, true))
				{
					possibleAcceptors.add(acceptor);
					acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
				}
			}
		}
	}

	@Override
	public synchronized void merge(FluidNetwork network)
	{
		if(network != null && network != this)
		{
			Set<FluidNetwork> networks = new HashSet<FluidNetwork>();
			networks.add(this);
			networks.add(network);
			FluidNetwork newNetwork = create(networks);
			newNetwork.refresh();
		}
	}
	
	public static class FluidTransferEvent extends Event
	{
		public final FluidNetwork fluidNetwork;
		
		public final FluidStack fluidType;
		public final boolean didTransfer;
		
		public FluidTransferEvent(FluidNetwork network, FluidStack type, boolean did)
		{
			fluidNetwork = network;
			fluidType = type;
			didTransfer = did;
		}
	}
	
	public float getScale()
	{
		return Math.min(1, (fluidStored == null || getCapacity() == 0 ? 0 : (float)fluidStored.amount/getCapacity()));
	}
		
	@Override
	public String toString()
	{
		return "[FluidNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}
	
	@Override
	protected FluidNetwork create(IGridTransmitter<FluidNetwork>... varTransmitters) 
	{
		FluidNetwork network = new FluidNetwork(varTransmitters);
		network.refFluid = refFluid;
		
		if(fluidStored != null)
		{
			if(network.fluidStored == null)
			{
				network.fluidStored = fluidStored;
			}
			else {
				network.fluidStored.amount += fluidStored.amount;
			}
		}
		
		network.fluidScale = network.getScale();
		
		fluidScale = 0;
		refFluid = null;
		fluidStored = null;
		
		return network;
	}

	@Override
	protected FluidNetwork create(Collection<IGridTransmitter<FluidNetwork>> collection) 
	{
		FluidNetwork network = new FluidNetwork(collection);
		network.refFluid = refFluid;
		
		if(fluidStored != null)
		{
			if(network.fluidStored == null)
			{
				network.fluidStored = fluidStored;
			}
			else {
				network.fluidStored.amount += fluidStored.amount;
			}
		}
		
		network.fluidScale = network.getScale();
		
		return network;
	}
	
	@Override
	public boolean canMerge(List<ITransmitterNetwork<?, ?>> networks)
	{
		Fluid found = null;
		
		for(ITransmitterNetwork<?, ?> network : networks)
		{
			if(network instanceof FluidNetwork)
			{
				FluidNetwork net = (FluidNetwork)network;
				
				if(net.fluidStored != null)
				{
					if(found != null && found != net.fluidStored.getFluid())
					{
						return false;
					}
					
					found = net.fluidStored.getFluid();
				}
			}
		}
		
		return true;
	}

	@Override
	protected FluidNetwork create(Set<FluidNetwork> networks) 
	{
		return new FluidNetwork(networks);
	}
	
	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.FLUID;
	}

	@Override
	public String getNeeded()
	{
		return "Fluid needed: " + (float)getFluidNeeded()/1000F + " buckets";
	}
	
	@Override
	public String getStored()
	{
		return fluidStored == null ? "None" : fluidStored.getFluid().getLocalizedName() + ", " + fluidStored.amount + "mB";
	}

	@Override
	public String getFlow()
	{
		return Integer.toString(prevTransferAmount);
	}
}
