package mekanism.common;

import java.util.ArrayList;

import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
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
		if(energyNetwork == null)
		{
			energyNetwork = new EnergyNetwork(this);
		}
		
		return energyNetwork;
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
		energyNetwork = network;
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
	public PowerReceiver getPowerReceiver() 
	{
		return powerProvider;
	}

	@Override
	public void doWork(PowerHandler workProvider) {}

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