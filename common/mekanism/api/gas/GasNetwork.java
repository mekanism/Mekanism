package mekanism.api.gas;

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
import mekanism.common.FluidNetwork;
import mekanism.common.util.ListUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.fluids.FluidStack;
import cpw.mods.fml.common.FMLCommonHandler;

public class GasNetwork extends DynamicNetwork<IGasHandler, GasNetwork>
{
	public static final int TUBE_GAS = 256;
	
	public int transferDelay = 0;
	
	public boolean didTransfer;
	public boolean prevTransfer;
	
	public float gasScale;
	public float prevScale;
	
	/** Sent from server to client, actual stored buffer scale */
	public float definedScale;
	
	public Gas refGas = null;
	
	public GasStack gasStored;
	
	public GasNetwork(ITransmitter<GasNetwork>... varPipes)
	{
		transmitters.addAll(Arrays.asList(varPipes));
		register();
	}
	
	public GasNetwork(Collection<ITransmitter<GasNetwork>> collection)
	{
		transmitters.addAll(collection);
		register();
	}
	
	public GasNetwork(Set<GasNetwork> networks)
	{
		for(GasNetwork net : networks)
		{
			if(net != null)
			{
				if(net.refGas != null && net.gasScale > gasScale)
				{
					refGas = net.refGas;
					gasScale = net.gasScale;
				}
				
				if(net.gasStored != null)
				{
					if(gasStored == null)
					{
						gasStored = net.gasStored;
					}
					else {
						gasStored.amount += net.gasStored.amount;
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
    public void onNetworksCreated(List<GasNetwork> networks)
    {
    	if(FMLCommonHandler.instance().getEffectiveSide().isServer())
    	{
    		if(gasStored != null)
    		{
		    	int[] caps = new int[networks.size()];
		    	int cap = 0;
		    	
		    	for(GasNetwork network : networks)
		    	{
		    		caps[networks.indexOf(network)] = network.getCapacity();
		    		cap += network.getCapacity();
		    	}
		    	
		    	gasStored.amount = Math.min(cap, gasStored.amount);
		    	
		    	int[] values = ListUtils.calcPercentInt(ListUtils.percent(caps), gasStored.amount);
		    	
		    	for(GasNetwork network : networks)
		    	{
		    		int index = networks.indexOf(network);
		    		
		    		if(values[index] > 0)
		    		{
		    			network.gasStored = new GasStack(gasStored.getGas(), values[index]);
		    		}
		    	}
    		}
    	}
    }
	
	public synchronized int getGasNeeded()
	{
		return getCapacity()-(gasStored != null ? gasStored.amount : 0);
	}
	
	public synchronized int tickEmit(GasStack stack)
	{
		List availableAcceptors = Arrays.asList(getAcceptors(stack.getGas()).toArray());
		
		Collections.shuffle(availableAcceptors);
		
		int toSend = stack.amount;
		int prevSending = toSend;
		
		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = toSend % divider;
			int sending = (toSend-remaining)/divider;
			
			for(Object obj : availableAcceptors)
			{
				if(obj instanceof IGasHandler)
				{
					IGasHandler acceptor = (IGasHandler)obj;
					
					int currentSending = sending;
					
					if(remaining > 0)
					{
						currentSending++;
						remaining--;
					}
					
					toSend -= acceptor.receiveGas(acceptorDirections.get(acceptor).getOpposite(), new GasStack(stack.getGas(), currentSending));
				}
			}
		}
		
		int sent = prevSending-toSend;
		
		if(sent > 0 && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			refGas = stack.getGas();
			didTransfer = true;
			transferDelay = 2;
		}
		
		return sent;
	}
	
	public synchronized int emit(GasStack stack)
	{
		if(refGas != null && refGas != stack.getGas())
		{
			return 0;
		}
		
		int toUse = Math.min(getGasNeeded(), stack.amount);
		
		if(gasStored == null)
		{
			gasStored = stack.copy();
			gasStored.amount = toUse;
		}
		else {
			gasStored.amount += toUse;
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
				MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, refGas != null ? refGas.getID() : -1, didTransfer, getScale()));
				needsUpdate = false;
			}
			
			prevTransfer = didTransfer;
			
			if(gasStored != null)
			{
				gasStored.amount -= tickEmit(gasStored);
				
				if(gasStored.amount <= 0)
				{
					gasStored = null;
				}
			}
		}
	}
	
	@Override
	public void clientTick()
	{
		super.clientTick();
		
		if(didTransfer && gasScale < 1)
		{
			gasScale = Math.max(definedScale, Math.min(1, gasScale+0.02F));
		}
		else if(!didTransfer && gasScale > 0)
		{
			gasScale = Math.max(definedScale, Math.max(0, gasScale-0.02F));
			
			if(gasScale == 0)
			{
				refGas = null;
			}
		}
	}
	
	@Override
	public synchronized Set<IGasHandler> getAcceptors(Object... data)
	{
		Gas type = (Gas)data[0];
		Set<IGasHandler> toReturn = new HashSet<IGasHandler>();
		
		for(IGasHandler acceptor : possibleAcceptors)
		{
			if(acceptorDirections.get(acceptor) == null)
			{
				continue;
			}
			
			if(acceptor.canReceiveGas(acceptorDirections.get(acceptor).getOpposite(), type))
			{
				toReturn.add(acceptor);
			}
		}
		
		return toReturn;
	}

	@Override
	public synchronized void refresh()
	{
		Set<ITransmitter<GasNetwork>> iterTubes = (Set<ITransmitter<GasNetwork>>)transmitters.clone();
		Iterator<ITransmitter<GasNetwork>> it = iterTubes.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			ITransmitter<GasNetwork> conductor = (ITransmitter<GasNetwork>)it.next();

			if(conductor == null || ((TileEntity)conductor).isInvalid())
			{
				it.remove();
				transmitters.remove(conductor);
			}
			else {
				conductor.setTransmitterNetwork(this);
			}
		}
		
		for(ITransmitter<GasNetwork> pipe : transmitters)
		{
			IGasHandler[] acceptors = GasTransmission.getConnectedAcceptors((TileEntity)pipe);
		
			for(IGasHandler acceptor : acceptors)
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
	public synchronized void merge(GasNetwork network)
	{
		if(network != null && network != this)
		{
			Set<GasNetwork> networks = new HashSet();
			networks.add(this);
			networks.add(network);
			GasNetwork newNetwork = create(networks);
			newNetwork.refresh();
		}
	}

	public static class GasTransferEvent extends Event
	{
		public final GasNetwork gasNetwork;
		
		public final int transferType;
		public final boolean didTransfer;
		public final float gasScale;
		
		public GasTransferEvent(GasNetwork network, int type, boolean did, float scale)
		{
			gasNetwork = network;
			transferType = type;
			didTransfer = did;
			gasScale = scale;
		}
	}
	
	public float getScale()
	{
		return (gasStored == null || getCapacity() == 0 ? 0 : gasStored.amount/getCapacity());
	}
	
	@Override
	public String toString()
	{
		return "[GasNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}
	
	@Override
	protected GasNetwork create(ITransmitter<GasNetwork>... varTransmitters) 
	{
		GasNetwork network = new GasNetwork(varTransmitters);
		network.refGas = refGas;
		network.gasScale = gasScale;
		
		if(gasStored != null)
		{
			if(network.gasStored == null)
			{
				network.gasStored = gasStored;
			}
			else {
				network.gasStored.amount += gasStored.amount;
			}
		}
		
		return network;
	}

	@Override
	protected GasNetwork create(Collection<ITransmitter<GasNetwork>> collection) 
	{
		GasNetwork network = new GasNetwork(collection);
		network.refGas = refGas;
		network.gasScale = gasScale;
		
		if(gasStored != null)
		{
			if(network.gasStored == null)
			{
				network.gasStored = gasStored;
			}
			else {
				network.gasStored.amount += gasStored.amount;
			}
		}
		
		return network;
	}

	@Override
	protected GasNetwork create(Set<GasNetwork> networks) 
	{
		return new GasNetwork(networks);
	}
	
	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.GAS;
	}
	
	@Override
	public String getNeeded()
	{
		return Integer.toString(getGasNeeded());
	}
	
	@Override
	public String getFlow()
	{
		return gasStored != null ? gasStored.getGas().getLocalizedName() + " (" + gasStored.amount + ")" : "None";
	}
}
