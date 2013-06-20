package mekanism.common;

import java.util.ArrayList;

import mekanism.api.IUniversalCable;
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
	
	/** The scale of the energy (0F -> 1F) currently inside this cable. */
	public float energyScale;
	
	public TileEntityUniversalCable()
	{
		powerProvider = new CablePowerProvider(this);
		powerProvider.configure(0, 0, 100, 0, 100);
	}
	
	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
		{
			if(energyScale > 0)
			{
				energyScale -= .01;
			}
		}
	}
	
	@Override
	public boolean canTransferEnergy(TileEntity fromTile)
	{
		return worldObj.getBlockPowerInput(xCoord, yCoord, zCoord) == 0;
	}
	
	@Override
	public void onTransfer()
	{
		energyScale = Math.min(1, energyScale+.02F);
	}
	
	@Override
	public boolean canUpdate()
	{
		return true;
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
		return canTransferEnergy(Object3D.get(this).getFromSide(from).getTileEntity(worldObj)) ? (int)Math.min(100, new EnergyTransferProtocol(this, this, ignored).neededEnergy()*Mekanism.TO_BC) : 0;
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
		CableUtils.emitEnergyFromAllSidesIgnore(quantity*Mekanism.FROM_BC, tileEntity, ignored);
	}
}