package mekanism.common;

import java.util.HashSet;

import mekanism.api.Object3D;
import mekanism.api.TransmitterNetworkRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityUniversalCable extends TileEntityTransmitter<EnergyNetwork> implements IUniversalCable, IPowerReceptor
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
				if(cable instanceof IUniversalCable && ((IUniversalCable)cable).getNetwork(false) != null)
				{
					connectedNets.add(((IUniversalCable)cable).getNetwork());
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
	public void setCachedEnergy(double scale)
	{
		energyScale = scale;
	}
	
	public float getEnergyScale()
	{
		return (float)energyScale;
	}
}