package mekanism.common;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.Pos3D;
import mekanism.api.lasers.ILaserReceptor;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

public class LaserManager
{
	public static MovingObjectPosition fireLaser(TileEntity from, EnumFacing direction, double energy, World world)
	{
		return fireLaser(new Pos3D(from).centre().translate(direction, 0.501), direction, energy, world);
	}

	public static MovingObjectPosition fireLaser(Pos3D from, EnumFacing direction, double energy, World world)
	{
		Pos3D to = from.clone().translate(direction, general.laserRange - 0.002);

		MovingObjectPosition mop = world.rayTraceBlocks(new Vec3(from.xPos, from.yPos, from.zPos), new Vec3(to.xPos, to.yPos, to.zPos));

		if(mop != null)
		{
			to = new Pos3D(mop.hitVec);
			BlockPos toCoord = mop.getBlockPos();
			TileEntity tile = world.getTileEntity(toCoord);

			if(tile instanceof ILaserReceptor)
			{
				if(!(((ILaserReceptor)tile).canLasersDig()))
				{
					((ILaserReceptor)tile).receiveLaserEnergy(energy, mop.sideHit);
				}
			}
		}

		from.translateExcludingSide(direction, -0.1);
		to.translateExcludingSide(direction, 0.1);

		for(Entity e : (List<Entity>)world.getEntitiesWithinAABB(Entity.class, Pos3D.getAABB(from, to)))
		{
			if(!e.isImmuneToFire()) e.setFire((int)(energy / 1000));
		}

		return mop;
	}

	public static List<ItemStack> breakBlock(Coord4D blockCoord, boolean dropAtBlock, World world)
	{
		List<ItemStack> ret = null;
		Block blockHit = blockCoord.getBlock(world);
		if(dropAtBlock)
		{
			blockHit.dropBlockAsItem(world, blockCoord, blockCoord.getBlockState(world), 0);
		}
		else
		{
			ret = blockHit.getDrops(world, blockCoord, blockCoord.getBlockState(world), 0);
		}
		blockHit.breakBlock(world, blockCoord, blockCoord.getBlockState(world));
		world.setBlockToAir(blockCoord);
		world.playAuxSFX(2001, blockCoord, Block.getIdFromBlock(blockHit));
		return ret;
	}

	public static void fireLaserClient(TileEntity from, EnumFacing direction, double energy, World world)
	{
		fireLaserClient(new Pos3D(from).centre().translate(direction, 0.501), direction, energy, world);
	}

	public static void fireLaserClient(Pos3D from, EnumFacing direction, double energy, World world)
	{
		Pos3D to = from.clone().translate(direction, general.laserRange - 0.002);
		MovingObjectPosition mop = world.rayTraceBlocks(new Vec3(from.xPos, from.yPos, from.zPos), new Vec3(to.xPos, to.yPos, to.zPos));

		if(mop != null)
		{
			to = new Pos3D(mop.hitVec);
		}
		from.translate(direction, -0.501);
		Mekanism.proxy.renderLaser(world, from, to, direction, energy);
	}

}
