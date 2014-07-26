package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.lasers.LaserManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityLaserAmplifier extends TileEntity implements ILaserReceptor
{
	public static final double MAX_ENERGY = 10000000;
	public double collectedEnergy = 0;
	public double threshold = 0;

	public int ticks;
	public int time;

	public ForgeDirection facing;


	public LaserEmitterMode mode;

	@Override
	public void receiveLaserEnergy(double energy, ForgeDirection side)
	{
		collectedEnergy += energy;
	}

	@Override
	public boolean canLasersDig()
	{
		return false;
	}

	@Override
	public double energyToDig()
	{
		return Double.MAX_VALUE;
	}

	@Override
	public void updateEntity()
	{
		if(shouldFire())
		{
			LaserManager.fireLaser(Coord4D.get(this), facing, collectedEnergy, worldObj);
		}
	}

	public boolean shouldFire()
	{
		switch(mode)
		{
			case THRESHOLD:
				return collectedEnergy > threshold;
			case REDSTONE:
				return false; //TODO implement
			case REDSTONE_PULSE:
				return false; // TODO implement
			case TIMER:
				return ticks > time;
		}
		return false;
	}

	public static enum LaserEmitterMode
	{
		THRESHOLD,
		REDSTONE,
		REDSTONE_PULSE,
		TIMER;
	}
}
