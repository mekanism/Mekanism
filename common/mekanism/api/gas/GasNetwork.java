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
	
	public int emit(int gasToSend, EnumGas transferType, TileEntity emitter)
	{
		List availableAcceptors = Arrays.asList(getAcceptors(transferType).toArray());
		
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
	
	@Override
	public Set<IGasAcceptor> getAcceptors(Object... data)
	{
		EnumGas transferType = (EnumGas)data[0];
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

	@Override
	public void refresh()
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
	public void merge(GasNetwork network)
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
		
		public final EnumGas transferType;
		
		public GasTransferEvent(GasNetwork network, EnumGas type)
		{
			gasNetwork = network;
			transferType = type;
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
		return "Not defined yet for Fluid networks";
	}
}
