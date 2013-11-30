package mekanism.common.tileentity;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.HashSet;

import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.EnergyNetwork;
import mekanism.common.Mekanism;
import mekanism.common.Object3D;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.grid.IElectricityNetwork;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cofh.api.energy.IEnergyHandler;

public class TileEntityUniversalCable extends TileEntityTransmitter<EnergyNetwork> implements IPowerReceptor, IEnergySink, IEnergyHandler, IElectrical
{
	/** A fake power handler used to initiate energy transfer calculations. */
	public PowerHandler powerHandler;
	
	/** A fake UE ElectricityNetwork used to accept power from EU machines */
	public IElectricityNetwork ueNetwork;
	
	public double energyScale;
	
	public TileEntityUniversalCable()
	{
		powerHandler = new PowerHandler(this, PowerHandler.Type.STORAGE);
		powerHandler.configurePowerPerdition(0, 0);
		powerHandler.configure(0, 0, 0, 0);
	}
	
	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ENERGY;
	}
	
	@Override
	public EnergyNetwork getTransmitterNetwork(boolean createIfNull)
	{
		if(theNetwork == null && createIfNull)
		{
			TileEntity[] adjacentCables = CableUtils.getConnectedCables(this);
			HashSet<EnergyNetwork> connectedNets = new HashSet<EnergyNetwork>();
			
			for(TileEntity cable : adjacentCables)
			{
				if(TransmissionType.checkTransmissionType(cable, TransmissionType.ENERGY) && ((ITransmitter<EnergyNetwork>)cable).getTransmitterNetwork(false) != null)
				{
					connectedNets.add(((ITransmitter<EnergyNetwork>)cable).getTransmitterNetwork());
				}
			}
			
			if(connectedNets.size() == 0)
			{
				theNetwork = new EnergyNetwork(this);
			}
			else if(connectedNets.size() == 1)
			{
				theNetwork = connectedNets.iterator().next();
				theNetwork.transmitters.add(this);
			}
			else {
				theNetwork = new EnergyNetwork(connectedNets);
				theNetwork.transmitters.add(this);
			}
		}
		
		return theNetwork;
	}
	
	@Override
	public void fixTransmitterNetwork()
	{
		getTransmitterNetwork().fixMessedUpNetwork(this);
	}
	
	@Override
	public void invalidate()
	{
		getTransmitterNetwork().split(this);
		
		if(!worldObj.isRemote)
		{
			TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
			
			Mekanism.ic2Registered.remove(Object3D.get(this));
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		}
		
		super.invalidate();
	}
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		getTransmitterNetwork().split(this);
		
		if(!worldObj.isRemote)
		{			
			Mekanism.ic2Registered.remove(Object3D.get(this));
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		}
	}
	
	@Override
	public void removeFromTransmitterNetwork()
	{
		if(theNetwork != null)
		{
			theNetwork.removeTransmitter(this);
		}
	}

	@Override
	public void refreshTransmitterNetwork() 
	{
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = Object3D.get(this).getFromSide(side).getTileEntity(worldObj);
			
			if(TransmissionType.checkTransmissionType(tileEntity, TransmissionType.ENERGY))
			{
				getTransmitterNetwork().merge(((ITransmitter<EnergyNetwork>)tileEntity).getTransmitterNetwork());
			}
		}
		
		getTransmitterNetwork().refresh();
		reconfigure();
	}
	
	@Override
	public void chunkLoad()
	{
		register();
	}
	
	public void register()
	{
		if(!worldObj.isRemote)
		{
			if(!Mekanism.ic2Registered.contains(Object3D.get(this)))
			{
				Mekanism.ic2Registered.add(Object3D.get(this));
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			}
		}
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) 
	{
		if(getTransmitterNetwork().getEnergyNeeded(new ArrayList()) == 0)
		{
			return null;
		}
		
		return powerHandler.getPowerReceiver();
	}
	
	@Override
	public World getWorld()
	{
		return worldObj;
	}
	
	private void reconfigure()
	{
		if(MekanismUtils.useBuildcraft())
		{
			float needed = (float)(getTransmitterNetwork().getEnergyNeeded(getBuildCraftIgnored())*Mekanism.TO_BC);
			powerHandler.configure(1, needed, 0, needed);
		}
	}
	
	public ArrayList<TileEntity> getBuildCraftIgnored()
	{
		ArrayList<TileEntity> ignored = new ArrayList<TileEntity>();
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = Object3D.get(this).getFromSide(side).getTileEntity(worldObj);
			
			if(tile != null)
			{
				if(powerHandler.powerSources[side.ordinal()] > 0)
				{
					ignored.add(tile);
				}
				else if(tile instanceof IPowerEmitter)
				{
					IPowerEmitter emitter = (IPowerEmitter)tile;
					
					if(emitter.canEmitPowerFrom(side.getOpposite()))
					{
						ignored.add(tile);
					}
				}
			}
		}
		
		return ignored;
	}

	@Override
	public void doWork(PowerHandler workProvider) 
	{
		if(MekanismUtils.useBuildcraft())
		{
			if(powerHandler.getEnergyStored() > 0)
			{
				getTransmitterNetwork().emit(powerHandler.getEnergyStored()*Mekanism.FROM_BC, getBuildCraftIgnored());
			}
			
			powerHandler.setEnergy(0);
			reconfigure();
		}
	}
	
	public void setCachedEnergy(double scale)
	{
		energyScale = scale;
	}
	
	public float getEnergyScale()
	{
		return (float)energyScale;
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return true;
	}

	@Override
	public double demandedEnergyUnits()
	{
		return getTransmitterNetwork().getEnergyNeeded(getBuildCraftIgnored())*Mekanism.TO_IC2;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		ArrayList list = new ArrayList();
		list.add(Object3D.get(this).getFromSide(direction).getTileEntity(worldObj));
    	return getTransmitterNetwork().emit(i*Mekanism.FROM_IC2, list)*Mekanism.TO_IC2;
    }

	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int getTransmitterNetworkSize()
	{
		return getTransmitterNetwork().getSize();
	}

	@Override
	public int getTransmitterNetworkAcceptorSize()
	{
		return getTransmitterNetwork().getAcceptorSize();
	}

	@Override
	public String getTransmitterNetworkNeeded()
	{
		return getTransmitterNetwork().getNeeded();
	}
	
	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlow();
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) 
	{
		ArrayList list = new ArrayList();
		list.add(Object3D.get(this).getFromSide(from).getTileEntity(worldObj));
		
		if(!simulate)
		{
	    	return maxReceive - (int)Math.round(getTransmitterNetwork().emit(maxReceive*Mekanism.FROM_TE, list)*Mekanism.TO_TE);
		}
		
		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) 
	{
		return 0;
	}

	@Override
	public boolean canInterface(ForgeDirection from) 
	{
		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) 
	{
		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		ArrayList list = new ArrayList();
		list.add(Object3D.get(this).getFromSide(from).getTileEntity(worldObj));
		return (int)Math.round(getTransmitterNetwork().getEnergyNeeded(list)*Mekanism.TO_TE);
	}
	
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive)
	{
		ArrayList list = new ArrayList();
		list.add(Object3D.get(this).getFromSide(from).getTileEntity(worldObj));
		
		if(doReceive && receive != null && receive.getWatts() > 0)
		{
			return receive.getWatts() - (float)(getTransmitterNetwork().emit(receive.getWatts()*Mekanism.FROM_UE, list));
		}
		
		return 0;
	}

	@Override
	public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide)
	{
		return null;
	}

	@Override
	public float getRequest(ForgeDirection direction)
	{
		ArrayList list = new ArrayList();
		list.add(Object3D.get(this).getFromSide(direction).getTileEntity(worldObj));
		return (float)(getTransmitterNetwork().getEnergyNeeded(list)*Mekanism.TO_UE);
	}

	@Override
	public float getProvide(ForgeDirection direction)
	{
		return 0;
	}

	@Override
	public float getVoltage()
	{
		return 120;
	}
}