package mekanism.common;

import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.tileentity.TileEntityUniversalCable;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityPack;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cpw.mods.fml.common.FMLCommonHandler;

public class EnergyNetwork extends DynamicNetwork<TileEntity, EnergyNetwork>
{	
	private double lastPowerScale = -1;
	private double joulesTransmitted = 0;
	private double joulesLastTick = 0;
	
	private boolean needsUpdate = false;
	
	public EnergyNetwork(ITransmitter<EnergyNetwork>... varCables)
	{
		transmitters.addAll(Arrays.asList(varCables));
		register();
	}
	
	public EnergyNetwork(Collection<ITransmitter<EnergyNetwork>> collection)
	{
		transmitters.addAll(collection);
		register();
	}
	
	public EnergyNetwork(Set<EnergyNetwork> networks)
	{
		for(EnergyNetwork net : networks)
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
	
	public synchronized double getEnergyNeeded(List<TileEntity> ignored)
	{
		double totalNeeded = 0;
		
		for(TileEntity acceptor : getAcceptors())
		{
			if(!ignored.contains(acceptor))
			{
				if(acceptor instanceof IStrictEnergyAcceptor)
				{
					totalNeeded += (((IStrictEnergyAcceptor)acceptor).getMaxEnergy() - ((IStrictEnergyAcceptor)acceptor).getEnergy());
				}
				else if(acceptor instanceof IEnergySink)
				{
					totalNeeded += Math.min((((IEnergySink)acceptor).demandedEnergyUnits()*Mekanism.FROM_IC2), (((IEnergySink)acceptor).getMaxSafeInput()*Mekanism.FROM_IC2));
				}
				else if(acceptor instanceof IPowerReceptor && MekanismUtils.useBuildcraft())
				{
					totalNeeded += (((IPowerReceptor)acceptor).getPowerReceiver(acceptorDirections.get(acceptor).getOpposite()).powerRequest()*Mekanism.FROM_BC);
				}
				else if(acceptor instanceof IElectrical)
				{
					totalNeeded += ((IElectrical)acceptor).getRequest(acceptorDirections.get(acceptor).getOpposite())*Mekanism.FROM_UE;
				}
			}
		}
		
		return totalNeeded;
	}
	
	public synchronized double emit(double energyToSend, ArrayList<TileEntity> ignored)
	{
		double energyAvailable = energyToSend;		
		double sent;
		
		List availableAcceptors = Arrays.asList(getAcceptors().toArray());

		Collections.shuffle(availableAcceptors);

		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			double remaining = energyToSend % divider;
			double sending = (energyToSend-remaining)/divider;

			for(Object obj : availableAcceptors)
			{
				if(obj instanceof TileEntity && !ignored.contains(obj))
				{
					TileEntity acceptor = (TileEntity)obj;
					double currentSending = sending+remaining;
					ForgeDirection side = acceptorDirections.get(acceptor);
					
					remaining = 0;
					
					if(acceptor instanceof IStrictEnergyAcceptor)
					{
						energyToSend -= (currentSending - ((IStrictEnergyAcceptor)acceptor).transferEnergyToAcceptor(currentSending));
					}
					else if(acceptor instanceof IEnergySink)
					{
						double toSend = Math.min(currentSending, (((IEnergySink)acceptor).getMaxSafeInput()*Mekanism.FROM_IC2));
						energyToSend -= (toSend - (((IEnergySink)acceptor).injectEnergyUnits(side.getOpposite(), toSend*Mekanism.TO_IC2)*Mekanism.FROM_IC2));
					}
					else if(acceptor instanceof IPowerReceptor && MekanismUtils.useBuildcraft())
					{
						PowerReceiver receiver = ((IPowerReceptor)acceptor).getPowerReceiver(side.getOpposite());
						
						if(receiver != null)
						{
			            	float toSend = receiver.receiveEnergy(Type.PIPE, (float)(Math.min(receiver.powerRequest(), currentSending*Mekanism.TO_BC)), side.getOpposite());
			            	energyToSend -= toSend*Mekanism.FROM_BC;
						}
					}
					else if(acceptor instanceof IElectrical)
					{
						double toSend = Math.min(currentSending, ((IElectrical)acceptor).getRequest(side.getOpposite())*Mekanism.FROM_UE);
						ElectricityPack pack = ElectricityPack.getFromWatts((float)(toSend*Mekanism.TO_UE), ((IElectrical)acceptor).getVoltage());
						energyToSend -= ((IElectrical)acceptor).receiveElectricity(side.getOpposite(), pack, true)*Mekanism.FROM_UE;
					}
				}
			}
			
			sent = energyAvailable - energyToSend;
			joulesTransmitted += sent;
		}
		
		return energyToSend;
	}
	
	@Override
	public synchronized Set<TileEntity> getAcceptors(Object... data)
	{
		Set<TileEntity> toReturn = new HashSet<TileEntity>();
		
		for(TileEntity acceptor : possibleAcceptors)
		{
			if(acceptor instanceof IStrictEnergyAcceptor)
			{
				if(((IStrictEnergyAcceptor)acceptor).canReceiveEnergy(acceptorDirections.get(acceptor).getOpposite()))
				{
					if((((IStrictEnergyAcceptor)acceptor).getMaxEnergy() - ((IStrictEnergyAcceptor)acceptor).getEnergy()) > 0)
					{
						toReturn.add(acceptor);
					}
				}
			}
			else if(acceptor instanceof IEnergySink)
			{
				if(((IEnergySink)acceptor).acceptsEnergyFrom(null, acceptorDirections.get(acceptor).getOpposite()))
				{
					if(Math.min((((IEnergySink)acceptor).demandedEnergyUnits()*Mekanism.FROM_IC2), (((IEnergySink)acceptor).getMaxSafeInput()*Mekanism.FROM_IC2)) > 0)
					{
						toReturn.add(acceptor);
					}
				}
			}
			else if(acceptor instanceof IPowerReceptor && MekanismUtils.useBuildcraft())
			{
				if(((IPowerReceptor)acceptor).getPowerReceiver(acceptorDirections.get(acceptor).getOpposite()) != null)
				{
					if((((IPowerReceptor)acceptor).getPowerReceiver(acceptorDirections.get(acceptor).getOpposite()).powerRequest()*Mekanism.FROM_BC) > 0)
					{
						TileEntityUniversalCable cable = (TileEntityUniversalCable)Object3D.get(acceptor).getFromSide(acceptorDirections.get(acceptor).getOpposite()).getTileEntity(acceptor.worldObj);
						
						if(cable != null && !cable.getBuildCraftIgnored().contains(acceptor))
						{
							toReturn.add(acceptor);
						}
					}
				}
			}
			else if(acceptor instanceof IElectrical)
			{
				if(((IElectrical)acceptor).canConnect(acceptorDirections.get(acceptor).getOpposite()))
				{
					if(((IElectrical)acceptor).getRequest(acceptorDirections.get(acceptor).getOpposite()) > 0)
					{
						toReturn.add(acceptor);
					}
				}
			}
		}
		
		return toReturn;
	}

	@Override
	public synchronized void refresh()
	{
		Set<ITransmitter<EnergyNetwork>> iterCables = (Set<ITransmitter<EnergyNetwork>>)transmitters.clone();
		Iterator<ITransmitter<EnergyNetwork>> it = iterCables.iterator();
		
		possibleAcceptors.clear();
		acceptorDirections.clear();

		while(it.hasNext())
		{
			ITransmitter<EnergyNetwork> conductor = (ITransmitter<EnergyNetwork>)it.next();

			if(conductor == null || ((TileEntity)conductor).isInvalid())
			{
				it.remove();
				transmitters.remove(conductor);
			}
			else {
				conductor.setTransmitterNetwork(this);
			}
		}
		
		for(ITransmitter<EnergyNetwork> cable : iterCables)
		{
			TileEntity[] acceptors = CableUtils.getConnectedEnergyAcceptors((TileEntity)cable);
		
			for(TileEntity acceptor : acceptors)
			{
				if(acceptor != null && !(acceptor instanceof ITransmitter))
				{
					possibleAcceptors.add(acceptor);
					acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
				}
			}
		}
		
		needsUpdate = true;
	}

	@Override
	public synchronized void merge(EnergyNetwork network)
	{
		if(network != null && network != this)
		{
			Set<EnergyNetwork> networks = new HashSet<EnergyNetwork>();
			networks.add(this);
			networks.add(network);
			EnergyNetwork newNetwork = new EnergyNetwork(networks);
			newNetwork.refresh();
		}
	}
	
	public static class EnergyTransferEvent extends Event
	{
		public final EnergyNetwork energyNetwork;
		
		public final double power;
		
		public EnergyTransferEvent(EnergyNetwork network, double currentPower)
		{
			energyNetwork = network;
			power = currentPower;
		}
	}

	@Override
	public String toString()
	{
		return "[EnergyNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
	}

	@Override
	public void tick()
	{
		clearJoulesTransmitted();
		
		super.tick();
		
		double currentPowerScale = getPowerScale();
		
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			if(currentPowerScale != lastPowerScale)
			{
				needsUpdate = true;
			}
			
			lastPowerScale = currentPowerScale;

			if(needsUpdate)
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTransferEvent(this, currentPowerScale));
				needsUpdate = false;
			}
		}
	}
	
	public double getPowerScale()
	{
		return joulesLastTick == 0 ? 0 : Math.min(Math.ceil(Math.log10(getPower())*2)/10, 1);
	}
	
	public void clearJoulesTransmitted()
	{
		joulesLastTick = joulesTransmitted;
		joulesTransmitted = 0;
	}
	
	public double getPower()
	{
		return joulesLastTick * 20;
	}
	
	@Override
	protected EnergyNetwork create(ITransmitter<EnergyNetwork>... varTransmitters) 
	{
		return new EnergyNetwork(varTransmitters);
	}

	@Override
	protected EnergyNetwork create(Collection<ITransmitter<EnergyNetwork>> collection) 
	{
		return new EnergyNetwork(collection);
	}

	@Override
	protected EnergyNetwork create(Set<EnergyNetwork> networks) 
	{
		return new EnergyNetwork(networks);
	}
	
	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ENERGY;
	}
	
	@Override
	public String getNeeded()
	{
		return ElectricityDisplay.getDisplay((float)(getEnergyNeeded(new ArrayList<TileEntity>())*Mekanism.TO_UE), ElectricityDisplay.ElectricUnit.JOULES);
	}

	@Override
	public String getFlow()
	{
		return ElectricityDisplay.getDisplay((float)(getPower()*Mekanism.TO_UE), ElectricityDisplay.ElectricUnit.JOULES);
	}
}
