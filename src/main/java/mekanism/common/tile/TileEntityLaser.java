package mekanism.common.tile;

import mekanism.api.Coord4D;
import mekanism.api.lasers.LaserManager;

import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityLaser extends TileEntityElectricBlock
{
	public static final double LASER_ENERGY = 50000;

	public TileEntityLaser()
	{
		super("Laser", 100000);
	}

	@Override
	public void onUpdate()
	{
		if(getEnergy() >= LASER_ENERGY)
		{
			LaserManager.fireLaser(Coord4D.get(this), ForgeDirection.getOrientation(facing), LASER_ENERGY);
		}
	}
}
