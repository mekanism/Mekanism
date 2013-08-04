package mekanism.common;

import java.util.HashSet;

import mekanism.api.Object3D;
import mekanism.api.TransmitterNetworkRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityUniversalCable extends TileEntity implements IUniversalCable, IPowerReceptor
{
	/** A fake power handler used to initiate energy transfer calculations. */
	public PowerHandler powerHandler;
	
	/** The energy network currently in use by this cable segment. */
	public EnergyNetwork energyNetwork;
	
	public TileEntityUniversalCable()
	{
		powerHandler = new PowerHandler(this, PowerHandler.Type.STORAGE);
		powerHandler.configure(0, 100, 0, 100);
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
	
	public float getEnergyScale()
	{
		//TODO: Let the client know how much power's being transferred
		return 1.F;
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
			if(connectedNets.size() == 0 || worldObj.isRemote)
			{
				energyNetwork = new EnergyNetwork(this);
			}
			else if(connectedNets.size() == 1)
			{
				energyNetwork = connectedNets.iterator().next();
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
		if(energyNetwork != null)
		{
			energyNetwork.removeCable(this);
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
				
				if(tileEntity instanceof IUniversalCable)
				{
					getNetwork().merge(((IUniversalCable)tileEntity).getNetwork());
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
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
	public void onChunkUnload() 
	{
		invalidate();
		TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
	}
}