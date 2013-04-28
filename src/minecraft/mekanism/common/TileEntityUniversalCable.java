package mekanism.common;

import java.util.ArrayList;

import mekanism.api.IUniversalCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.power.PowerProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityUniversalCable extends TileEntity implements IUniversalCable, IPowerReceptor
{
	/** A fake power provider used to initiate energy transfer calculations. */
	public CablePowerProvider powerProvider;
	
	/** The scale of the energy (0F -> 1F) currently inside this cable. */
	public float energyScale;
	
	/** This cable's previous energy scale state. */
	public float prevScale;
	
	public TileEntityUniversalCable()
	{
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = new CablePowerProvider(this);
			powerProvider.configure(0, 0, 100, 0, 100);
		}
	}
	
	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
		{
			if(Math.abs((energyScale-prevScale)*15F) >= 1)
			{
				worldObj.updateAllLightTypes(xCoord, yCoord, zCoord);
				prevScale = energyScale;
			}
			
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
		ignored.add(VectorHelper.getTileEntityFromSide(worldObj, new Vector3(xCoord, yCoord, zCoord), from));
		return canTransferEnergy(VectorHelper.getTileEntityFromSide(worldObj, new Vector3(xCoord, yCoord, zCoord), from)) ? (int)Math.min(100, new EnergyTransferProtocol(this, this, ignored).neededEnergy()) : 0;
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
		ignored.add(VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), from));
		CableUtils.emitEnergyFromAllSidesIgnore(quantity*Mekanism.FROM_BC, tileEntity, ignored);
	}
}