package mekanism.common.tileentity;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;

import java.util.HashSet;

import mekanism.api.Coord4D;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.common.EnergyNetwork;
import mekanism.common.Mekanism;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cofh.api.energy.IEnergyHandler;

public class TileEntityUniversalCable extends TileEntityTransmitter<EnergyNetwork> implements IPowerReceptor, IEnergySink, IEnergyHandler
{
	/** A fake power handler used to initiate energy transfer calculations. */
	public PowerHandler powerHandler;
	
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
				if(TransmissionType.checkTransmissionType(cable, TransmissionType.ENERGY) && ((IGridTransmitter<EnergyNetwork>)cable).getTransmitterNetwork(false) != null)
				{
					connectedNets.add(((IGridTransmitter<EnergyNetwork>)cable).getTransmitterNetwork());
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
			
			Mekanism.ic2Registered.remove(Coord4D.get(this));
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
			Mekanism.ic2Registered.remove(Coord4D.get(this));
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
			TileEntity tileEntity = Coord4D.get(this).getFromSide(side).getTileEntity(worldObj);
			
			if(TransmissionType.checkTransmissionType(tileEntity, TransmissionType.ENERGY))
			{
				getTransmitterNetwork().merge(((IGridTransmitter<EnergyNetwork>)tileEntity).getTransmitterNetwork());
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
			if(!Mekanism.ic2Registered.contains(Coord4D.get(this)))
			{
				Mekanism.ic2Registered.add(Coord4D.get(this));
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			}
			
			getTransmitterNetwork().refresh();
		}
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) 
	{
		if(getTransmitterNetwork().getEnergyNeeded() == 0)
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
			float needed = (float)(getTransmitterNetwork().getEnergyNeeded()*Mekanism.TO_BC);
			powerHandler.configure(1, needed, 0, needed);
		}
	}

	@Override
	public void doWork(PowerHandler workProvider) 
	{
		if(MekanismUtils.useBuildcraft())
		{
			if(powerHandler.getEnergyStored() > 0)
			{
				getTransmitterNetwork().emit(powerHandler.getEnergyStored()*Mekanism.FROM_BC);
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
		return getTransmitterNetwork().getEnergyNeeded()*Mekanism.TO_IC2;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
    	return getTransmitterNetwork().emit(i*Mekanism.FROM_IC2)*Mekanism.TO_IC2;
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
		if(!simulate)
		{
	    	return maxReceive - (int)Math.round(getTransmitterNetwork().emit(maxReceive*Mekanism.FROM_TE)*Mekanism.TO_TE);
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
		return (int)Math.round(getTransmitterNetwork().getEnergyNeeded()*Mekanism.TO_TE);
	}

    @Override
    public int getCapacity()
    {
        return 10000;
    }
}