package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.tileentity.TileEntityMechanicalPipe;
import mekanism.common.util.ListUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class FluidNetwork extends DynamicNetwork<IFluidHandler, FluidNetwork>
{
	public static final int PIPE_FLUID = 1000;
	
	public int transferDelay = 0;
	
	public boolean didTransfer;
	public boolean prevTransfer;
	
	public float fluidScale;
	public float prevScale;
	
	/** Sent from server to client, actual stored buffer scale */
	public float definedScale;
	
	public Fluid refFluid = null;
	
	public FluidStack fluidStored;
	
	public FluidNetwork(ITransmitter<FluidNetwork>... varPipes)
	{
		transmitters.addAll(Arrays.asList(varPipes));
		register();
	}
	
	public FluidNetwork(Collection<ITransmitter<FluidNetwork>> collection)
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
				if(net.refFluid != null && net.fluidScale > fluidScale)
				{
					refFluid = net.refFluid;
					fluidScale = net.fluidScale;
				}
				
				if(net.fluidStored != null)
				{
					if(fluidStored == null)
					{
						fluidStored = net.fluidStored;
					}
					else {
						fluidStored.amount += net.fluidStored.amount;
					}
				}
				
				addAllTransmitters(net.transmitters);
				net.deregister();
			}
		}
		
		refresh();
		register();
	}
	
    @Override
    public void onNetworksCreated(List<FluidNetwork> networks)
    {
    	if(FMLCommonHandler.instance().getEffectiveSide().isServer())
    	{
    		if(fluidStored != null)
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
		    		}
		    	}
    		}
    	}
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
	public void tick()
	{
		super.tick();
		
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			if(transferDelay == 0)
			{
				didTransfer = false;
			}
			else {
				transferDelay--;
			}
			
			if(Math.abs(getScale()-prevScale) > 0.01 || (getScale() != prevScale && (getScale() == 0 || getScale() == 1)))
			{
				needsUpdate = true;
			}
			
			prevScale = getScale();
			
			if(didTransfer != prevTransfer || needsUpdate)
			{
				MinecraftForge.EVENT_BUS.post(new FluidTransferEvent(this, fluidStored != null ? fluidStored.getFluid().getID() : -1, didTransfer, getScale()));
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
		
		fluidScale = Math.max(fluidScale, definedScale);
		
		if(didTransfer && fluidScale < 1)
		{
			fluidScale = Math.max(definedScale, Math.min(1, fluidScale+0.02F));
		}
		else if(!didTransfer && fluidScale > 0)
		{
			fluidScale = Math.max(definedScale, Math.max(0, fluidScale-0.02F));
			
			if(fluidScale == 0)
			{
				refFluid = null;
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
		Set<ITransmitter<FluidNetwork>> iterPipes = (Set<ITransmitter<FluidNetwork>>)transmitters.clone();
		Iterator it = iterPipes.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			ITransmitter<FluidNetwork> conductor = (ITransmitter<FluidNetwork>)it.next();

			if(conductor == null || ((TileEntity)conductor).isInvalid())
			{
				it.remove();
				transmitters.remove(conductor);
			}
			else {
				conductor.setTransmitterNetwork(this);
			}
		}
		
		for(ITransmitter<FluidNetwork> pipe : iterPipes)
		{
			if(pipe instanceof TileEntityMechanicalPipe && ((TileEntityMechanicalPipe)pipe).isActive) continue;
			
			IFluidHandler[] acceptors = PipeUtils.getConnectedAcceptors((TileEntity)pipe);
		
			for(IFluidHandler acceptor : acceptors)
			{
				if(acceptor != null && !(acceptor instanceof ITransmitter))
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
		
		public final int fluidType;
		public final boolean didTransfer;
		public final float fluidScale;
		
		public FluidTransferEvent(FluidNetwork network, int type, boolean did, float scale)
		{
			fluidNetwork = network;
			fluidType = type;
			didTransfer = did;
			fluidScale = scale;
		}
	}
	
	public float getScale()
	{
		return (fluidStored == null || getCapacity() == 0 ? 0 : (float)fluidStored.amount/getCapacity());
	}
		
	@Override
	public String toString()
	{
		return "[FluidNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}
	
	@Override
	protected FluidNetwork create(ITransmitter<FluidNetwork>... varTransmitters) 
	{
		FluidNetwork network = new FluidNetwork(varTransmitters);
		network.refFluid = refFluid;
		network.fluidScale = fluidScale;
		
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
		
		return network;
	}

	@Override
	protected FluidNetwork create(Collection<ITransmitter<FluidNetwork>> collection) 
	{
		FluidNetwork network = new FluidNetwork(collection);
		network.refFluid = refFluid;
		network.fluidScale = fluidScale;
		
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
		
		return network;
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
		return "Fluid needed (any type): " + (float)getFluidNeeded()/1000F + " buckets";
	}
	
	@Override
	public String getFlow()
	{
		return fluidStored == null ? "None" : fluidStored.getFluid().getLocalizedName() + ", " + fluidStored.amount + "mB/tick";
	}
}
