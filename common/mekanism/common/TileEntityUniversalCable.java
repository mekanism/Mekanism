package mekanism.common;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;

import java.util.ArrayList;
import java.util.HashSet;

import mekanism.api.ITransmitter;
import mekanism.api.Object3D;
import mekanism.api.TransmissionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;

public class TileEntityUniversalCable extends TileEntityTransmitter<EnergyNetwork> implements IPowerReceptor, IEnergyTile, IEnergySink
{
	/** A fake power handler used to initiate energy transfer calculations. */
	public PowerHandler powerHandler;
	
	public double energyScale;
	
	public TileEntityUniversalCable()
	{
		powerHandler = new PowerHandler(this, PowerHandler.Type.STORAGE);
		powerHandler.configure(0, 100, 0, 100);
	}
	
	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ENERGY;
	}
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	@Override
	public EnergyNetwork getNetwork(boolean createIfNull)
	{
		if(theNetwork == null && createIfNull)
		{
			TileEntity[] adjacentCables = CableUtils.getConnectedCables(this);
			HashSet<EnergyNetwork> connectedNets = new HashSet<EnergyNetwork>();
			
			for(TileEntity cable : adjacentCables)
			{
				if(MekanismUtils.checkTransmissionType(cable, TransmissionType.ENERGY) && ((ITransmitter<EnergyNetwork>)cable).getNetwork(false) != null)
				{
					connectedNets.add(((ITransmitter<EnergyNetwork>)cable).getNetwork());
				}
			}
			
			if(connectedNets.size() == 0 || worldObj.isRemote)
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
	public void fixNetwork()
	{
		getNetwork().fixMessedUpNetwork(this);
	}
	
	@Override
	public void invalidate()
	{
		if(!worldObj.isRemote)
		{
			getNetwork().split(this);
		}
		
		super.invalidate();
	}
	
	@Override
	public void removeFromNetwork()
	{
		if(theNetwork != null)
		{
			theNetwork.removeTransmitter(this);
		}
	}

	@Override
	public void refreshNetwork() 
	{
		if(!worldObj.isRemote)
		{
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = Object3D.get(this).getFromSide(side).getTileEntity(worldObj);
				
				if(MekanismUtils.checkTransmissionType(tileEntity, TransmissionType.ENERGY))
				{
					getNetwork().merge(((ITransmitter<EnergyNetwork>)tileEntity).getNetwork());
				}
			}
			
			getNetwork().refresh();
		}
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) 
	{
		return powerHandler.getPowerReceiver();
	}
	
	@Override
	public World getWorld()
	{
		return worldObj;
	}

	@Override
	public void doWork(PowerHandler workProvider) {}
	
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
		return getNetwork().getEnergyNeeded(new ArrayList())*Mekanism.TO_IC2;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		ArrayList list = new ArrayList();
		list.add(Object3D.get(this).getFromSide(direction).getTileEntity(worldObj));
    	return getNetwork().emit(i, list);
    }

	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
	public int getNetworkSize()
	{
		return getNetwork().getSize();
	}

	@Override
	public int getNetworkAcceptorSize()
	{
		return getNetwork().getAcceptorSize();
	}

	@Override
	public String getNetworkNeeded()
	{
		return getNetwork().getNeeded();
	}
	
	@Override
	public String getNetworkFlow()
	{
		return getNetwork().getFlow();
	}
}