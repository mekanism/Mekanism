package mekanism.api.lasers;

import mekanism.api.Coord4D;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class LaserManager
{
	public static int range = 100;

	public static void fireLaser(Coord4D from, ForgeDirection direction, double energy, World world)
	{
		Coord4D to = from.getFromSide(direction, range);
		MovingObjectPosition mop = world.rayTraceBlocks(Vec3.createVectorHelper(from.xCoord, from.yCoord, from.zCoord), Vec3.createVectorHelper(to.xCoord, to.yCoord, to.zCoord));

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

			renderLaser(from, new Coord4D(mop.blockX, mop.blockY, mop.blockZ));
		}
		else
		{
			renderLaser(from, to);
		}
	}

	public static void renderLaser(Coord4D from, Coord4D to)
	{
		//TODO Particle effects
	}
}
