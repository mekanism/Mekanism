package mekanism.common.tileentity;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import universalelectricity.core.block.IConductor;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.grid.IElectricityNetwork;
import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.EnergyNetwork;
import mekanism.common.Mekanism;
import mekanism.common.util.CableUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;

public class TileEntityUniversalCable extends TileEntityTransmitter<EnergyNetwork> implements IPowerReceptor, IEnergyTile, IEnergySink, IConductor
{
	/** A fake power handler used to initiate energy transfer calculations. */
	public PowerHandler powerHandler;
	
	/** A fake UE ElectricityNetwork used to accept power from EU machines */
	public IElectricityNetwork ueNetwork;
	
	public double energyScale;
	
	public TileEntityUniversalCable()
	{
		ueNetwork = new FakeUENetwork();
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
	public void fixTransmitterNetwork()
	{
		getTransmitterNetwork().fixMessedUpNetwork(this);
	}
	
	@Override
	public void invalidate()
	{
		if(!worldObj.isRemote)
		{
			getTransmitterNetwork().split(this);
		}
		
		super.invalidate();
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
		if(!worldObj.isRemote)
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
		return getTransmitterNetwork().getEnergyNeeded(new ArrayList())*Mekanism.TO_IC2;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		ArrayList list = new ArrayList();
		list.add(Object3D.get(this).getFromSide(direction).getTileEntity(worldObj));
    	return getTransmitterNetwork().emit(i, list);
    }

	@Override
	public int getMaxSafeInput()
	{
		return 2048;
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
	public IElectricityNetwork getNetwork()
	{
		return ueNetwork;
	}

	@Override
	public void setNetwork(IElectricityNetwork network) {}

	@Override
	public TileEntity[] getAdjacentConnections()
	{
		return new TileEntity[6];
	}

	@Override
	public void refresh()
	{
		refreshTransmitterNetwork();
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	@Override
	public float getResistance()
	{
		return 0;
	}

	@Override
	public float getCurrentCapacity()
	{
		return Integer.MAX_VALUE;
	}
	
	public class FakeUENetwork implements IElectricityNetwork
	{
		
		@Override
		public void split(IConductor connection) {}
		
		@Override
		public void refresh() {}
		
		@Override
		public void merge(IElectricityNetwork network) {}
		
		@Override
		public ArrayList<ForgeDirection> getPossibleDirections(TileEntity tile)
		{
			return new ArrayList<ForgeDirection>();
		}
		
		@Override
		public Set<IConductor> getConductors()
		{
			return new HashSet<IConductor>();
		}
		
		@Override
		public Set<TileEntity> getAcceptors()
		{
			return getTransmitterNetwork().getAcceptors();
		}
		
		@Override
		public float produce(ElectricityPack electricityPack, TileEntity... ignoreTiles)
		{
			ArrayList<TileEntity> ignore = new ArrayList<TileEntity>();
			ignore.addAll(Arrays.asList(ignoreTiles));
			double energy = electricityPack.getWatts() * Mekanism.FROM_UE;
			return (float)getTransmitterNetwork().emit(energy, ignore);
		}
		
		@Override
		public float getTotalResistance()
		{
			return 0;
		}
		
		@Override
		public ElectricityPack getRequest(TileEntity... ignoreTiles)
		{
			return ElectricityPack.getFromWatts((float)getTransmitterNetwork().getEnergyNeeded(new ArrayList<TileEntity>()), 0.12F);
		}
		
		@Override
		public float getLowestCurrentCapacity()
		{
			return Integer.MAX_VALUE;
		}
	}
}