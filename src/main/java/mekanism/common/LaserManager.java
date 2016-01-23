package mekanism.common;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.Pos3D;
import mekanism.api.lasers.ILaserReceptor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent;

public class LaserManager
{
	public static LaserInfo fireLaser(TileEntity from, EnumFacing direction, double energy, World world)
	{
		return fireLaser(new Pos3D(from).centre().translate(direction, 0.501), direction, energy, world);
	}

	public static LaserInfo fireLaser(Pos3D from, EnumFacing direction, double energy, World world)
	{
		Pos3D to = from.clone().translate(direction, general.laserRange - 0.002);

		MovingObjectPosition mop = world.rayTraceBlocks(from, to);

		if(mop != null)
		{
			to = new Pos3D(mop.hitVec);
			Coord4D toCoord = new Coord4D(mop.getBlockPos());
			TileEntity tile = toCoord.getTileEntity(world);

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
		
		boolean foundEntity = false;

		for(Entity e : (List<Entity>)world.getEntitiesWithinAABB(Entity.class, Pos3D.getAABB(from, to)))
		{
			foundEntity = true;
			
			if(!e.isImmuneToFire()) 
			{
				e.setFire((int)(energy / 1000));
			}
			
			if(energy > 256)
			{
				e.attackEntityFrom(DamageSource.generic, (float)energy/1000F);
			}
		}
		
		return new LaserInfo(mop, foundEntity);
	}

	public static List<ItemStack> breakBlock(Coord4D blockCoord, boolean dropAtBlock, World world)
	{
		if(!general.aestheticWorldDamage)
		{
			return null;
		}
		
		List<ItemStack> ret = null;
		IBlockState state = blockCoord.getBlockState(world);
		Block blockHit = state.getBlock();
		
		EntityPlayer dummy = Mekanism.proxy.getDummyPlayer((WorldServer)world, blockCoord.getX(), blockCoord.getY(), blockCoord.getZ()).get();
		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, blockCoord, state, dummy);
		MinecraftForge.EVENT_BUS.post(event);
		
		if(event.isCanceled())
		{
			return null;
		}
		
		if(dropAtBlock)
		{
			blockHit.dropBlockAsItem(world, blockCoord, state, 0);
		}
		else {
			ret = blockHit.getDrops(world, blockCoord, state, 0);
		}
		
		blockHit.breakBlock(world, blockCoord, state);
		world.setBlockToAir(blockCoord);
		world.playAuxSFX(2001, blockCoord, Block.getIdFromBlock(blockHit));
		
		return ret;
	}

	public static MovingObjectPosition fireLaserClient(TileEntity from, EnumFacing direction, double energy, World world)
	{
		return fireLaserClient(new Pos3D(from).centre().translate(direction, 0.501), direction, energy, world);
	}

	public static MovingObjectPosition fireLaserClient(Pos3D from, EnumFacing direction, double energy, World world)
	{
		Pos3D to = from.clone().translate(direction, general.laserRange - 0.002);
		MovingObjectPosition mop = world.rayTraceBlocks(from, to);

		if(mop != null)
		{
			to = new Pos3D(mop.hitVec);
		}
		
		from.translate(direction, -0.501);
		Mekanism.proxy.renderLaser(world, from, to, direction, energy);
		
		return mop;
	}
	
	public static class LaserInfo
	{
		public MovingObjectPosition movingPos;
		
		public boolean foundEntity;
		
		public LaserInfo(MovingObjectPosition mop, boolean b)
		{
			movingPos = mop;
			foundEntity = b;
		}
	}
}