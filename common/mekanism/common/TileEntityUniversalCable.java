package mekanism.common;

import java.util.ArrayList;
import java.util.HashSet;

import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityUniversalCable extends TileEntity implements IUniversalCable, IPowerReceptor
{
	/** A fake power provider used to initiate energy transfer calculations. */
	public CablePowerProvider powerProvider;
	
	/** The energy network currently in use by this cable segment. */
	public EnergyNetwork energyNetwork;
	
	public TileEntityUniversalCable()
	{
		powerProvider = new CablePowerProvider(this);
		powerProvider.configure(0, 0, 100, 0, 100);
	}
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	@Override
	public EnergyNetwork getNetwork()
	{
		return getNetwork(true);
	}
	
	@Override
	public EnergyNetwork getNetwork(boolean createIfNull)
	{
		if(energyNetwork == null && createIfNull)
		{
			TileEntity[] adjacentCables = CableUtils.getConnectedCables(this);
			HashSet<EnergyNetwork> connectedNets = new HashSet<EnergyNetwork>();
			for(TileEntity cable : adjacentCables)
			{
				if(cable instanceof IUniversalCable && ((IUniversalCable)cable).getNetwork(false) != null)
				{
					connectedNets.add(((IUniversalCable)cable).getNetwork());
				}
			}
			if(connectedNets.size() == 0)
			{
				energyNetwork = new EnergyNetwork(this);
			}
			else if(connectedNets.size() == 1)
			{
				energyNetwork = (EnergyNetwork)connectedNets.toArray()[0];
				energyNetwork.cables.add(this);
			}
			else {
				energyNetwork = new EnergyNetwork(connectedNets);
				energyNetwork.cables.add(this);
			}
		}
		
		return energyNetwork;
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
	public void setNetwork(EnergyNetwork network)
	{
		if(network != energyNetwork)
		{
			removeFromNetwork();
			energyNetwork = network;
		}
	}
	
	@Override
	public void removeFromNetwork()
	{
		energyNetwork.removeCable(this);
	}

	@Override
	public void refreshNetwork() 
	{
		if(!worldObj.isRemote)
		{
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = Object3D.get(this).getFromSide(side).getTileEntity(worldObj);
				
				if(tileEntity instanceof IUniversalCable)
				{
					getNetwork().merge(((IUniversalCable)tileEntity).getNetwork());
				}
			}
			
			getNetwork().refresh();
		}
	}

	@Override
	public void setPowerProvider(IPowerProvider provider) {}

	@Override
	public IPowerProvider getPowerProvider() 
	{
		return powerProvider;
	}

	@Override
	public void doWork() {}

	@Override
	public int powerRequest(ForgeDirection from)
	{
		ArrayList<TileEntity> ignored = new ArrayList<TileEntity>();
		ignored.add(Object3D.get(this).getFromSide(from).getTileEntity(worldObj));
		return (int)Math.min(100, getNetwork().getEnergyNeeded(ignored)*Mekanism.TO_BC);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
	public void onChunkUnload() {
		invalidate();
		EnergyNetworkRegistry.getInstance().pruneEmptyNetworks();
	}
}

class CablePowerProvider extends PowerProvider
{
	public TileEntity tileEntity;
	
	public CablePowerProvider(TileEntity tile)
	{
		tileEntity = tile;
	}
	
	@Override
	public void receiveEnergy(float quantity, ForgeDirection from)
	{
		ArrayList<TileEntity> ignored = new ArrayList<TileEntity>();
		ignored.add(Object3D.get(tileEntity).getFromSide(from).getTileEntity(tileEntity.worldObj));
		CableUtils.emitEnergyFromAllSides(quantity*Mekanism.FROM_BC, tileEntity, ignored);
	}
}