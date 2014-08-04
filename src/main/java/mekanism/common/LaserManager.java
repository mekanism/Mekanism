package mekanism.common;

import mekanism.api.Coord4D;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.Mekanism;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class LaserManager
{
	public static int range = 100;

	public static void fireLaser(Coord4D from, ForgeDirection direction, double energy, World world)
	{
		Coord4D rangeFrom = from.getFromSide(direction, 1);
		Coord4D to = from.getFromSide(direction, range);
		MovingObjectPosition mop = world.rayTraceBlocks(Vec3.createVectorHelper(rangeFrom.xCoord, rangeFrom.yCoord, rangeFrom.zCoord), Vec3.createVectorHelper(to.xCoord, to.yCoord, to.zCoord));

		if(mop != null)
		{
			TileEntity tile = world.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);

			if(tile instanceof ILaserReceptor)
			{
				if(((ILaserReceptor)tile).canLasersDig() && energy > ((ILaserReceptor)tile).energyToDig())
				{
					//TODO dig block
				}
				else
				{
					((ILaserReceptor)tile).receiveLaserEnergy(energy, ForgeDirection.getOrientation(mop.sideHit));
				}
			}
		}
	}

	public static void fireLaserClient(Coord4D from, ForgeDirection direction, World world)
	{
		Coord4D rangeFrom = from.getFromSide(direction, 1);
		Coord4D to = from.getFromSide(direction, range);
		MovingObjectPosition mop = world.rayTraceBlocks(Vec3.createVectorHelper(rangeFrom.xCoord, rangeFrom.yCoord, rangeFrom.zCoord), Vec3.createVectorHelper(to.xCoord, to.yCoord, to.zCoord));

		if(mop != null)
		{
			Mekanism.proxy.renderLaser(world, from, new Coord4D(mop.blockX, mop.blockY, mop.blockZ), direction);
		}
		else
		{
			Mekanism.proxy.renderLaser(world, from, to, direction);
		}
	}

}
