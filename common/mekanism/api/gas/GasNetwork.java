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
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import cpw.mods.fml.common.FMLCommonHandler;

public class GasNetwork extends DynamicNetwork<IGasAcceptor, GasNetwork>
{
	public int transferDelay = 0;
	
	public float gasScale;
	public float prevGasScale;
	
	public Gas refGas = null;
	
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
				addAllTransmitters(net.transmitters);
				net.deregister();
			}
		}
		
		refresh();
		register();
	}
	
	public synchronized int emit(GasStack stack, TileEntity emitter)
	{
		if(refGas != null && refGas != stack.getGas())
		{
			return 0;
		}
		
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
				if(obj instanceof IGasAcceptor && obj != emitter)
				{
					IGasAcceptor acceptor = (IGasAcceptor)obj;
					
					int currentSending = sending;
					
					if(remaining > 0)
					{
						currentSending++;
						remaining--;
					}
					
					toSend -= acceptor.receiveGas(new GasStack(stack.getGas(), currentSending));
				}
			}
		}
		
		int sent = prevSending-toSend;
		
		if(sent > 0 && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			if(stack.getGas() == refGas)
			{
				gasScale = Math.min(1, gasScale+((float)sent/100));
			}
			else if(refGas == null)
			{
				refGas = stack.getGas();
				gasScale = Math.min(1, ((float)sent/100));
			}
			
			transferDelay = 2;
		}
		
		return sent;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			if(transferDelay == 0)
			{
				if(gasScale > 0)
				{
					gasScale = Math.max(0, gasScale-.02F);
				}
				else {
					refGas = null;
				}
			}
			else {
				transferDelay--;
			}
			
			if(gasScale != prevGasScale)
			{
				MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this, refGas != null ? refGas.getID() : -1, gasScale));
			}
			
			prevGasScale = gasScale;
		}
	}
	
	@Override
	public synchronized Set<IGasAcceptor> getAcceptors(Object... data)
	{
		Gas type = (Gas)data[0];
		Set<IGasAcceptor> toReturn = new HashSet<IGasAcceptor>();
		
		for(IGasAcceptor acceptor : possibleAcceptors)
		{
			if(acceptor.canReceiveGas(acceptorDirections.get(acceptor).getOpposite(), type))
			{
				int stored = ((IGasStorage)acceptor).getGas() != null ? ((IGasStorage)acceptor).getGas().amount : 0;
				
				if(!(acceptor instanceof IGasStorage) || (acceptor instanceof IGasStorage && (((IGasStorage)acceptor).getMaxGas() - stored) > 0))
				{
					toReturn.add(acceptor);
				}
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
			IGasAcceptor[] acceptors = GasTransmission.getConnectedAcceptors((TileEntity)pipe);
		
			for(IGasAcceptor acceptor : acceptors)
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
			GasNetwork newNetwork = new GasNetwork(networks);
			newNetwork.refresh();
		}
	}

	public static class GasTransferEvent extends Event
	{
		public final GasNetwork gasNetwork;
		
		public final int transferType;
		public final float gasScale;
		
		public GasTransferEvent(GasNetwork network, int type, float scale)
		{
			gasNetwork = network;
			transferType = type;
			gasScale = scale;
		}
	}
	
	@Override
	public String toString()
	{
		return "[GasNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}
	
	@Override
	protected GasNetwork create(ITransmitter<GasNetwork>... varTransmitters) 
	{
		return new GasNetwork(varTransmitters);
	}

	@Override
	protected GasNetwork create(Collection<ITransmitter<GasNetwork>> collection) 
	{
		return new GasNetwork(collection);
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
		return "Undefined for Gas networks.";
	}
	
	@Override
	public String getFlow()
	{
		return "Not defined yet for Gas networks";
	}
}
