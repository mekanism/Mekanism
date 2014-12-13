package mekanism.client;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.tile.TileEntityMultiblock;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThreadMultiblockSparkle extends Thread
{
	public TileEntityMultiblock<?> pointer;

	public Random random = new Random();

	public Set<TileEntity> iteratedNodes = new HashSet<TileEntity>();

	public ThreadMultiblockSparkle(TileEntityMultiblock<?> tileEntity)
	{
		pointer = tileEntity;
	}

	@Override
	public void run()
	{
		try {
			if(general.dynamicTankEasterEgg)
			{
				pointer.getWorldObj().playSound(pointer.getPos().getX(), pointer.getPos().getY(), pointer.getPos().getZ(), "mekanism:etc.cj", 1F, 1F, false);
			}

			loop(pointer);
		} catch(Exception e) {}
	}

	public void loop(TileEntityMultiblock<?> tileEntity)
	{
		World world = pointer.getWorldObj();

		for(int i = 0; i < 6; i++)
		{
			if(Coord4D.get(tileEntity).sideVisible(EnumFacing.DOWN, world))
			{
				world.spawnParticle("reddust", tileEntity.getPos().getX() + random.nextDouble(), tileEntity.getPos().getY() + -.01, tileEntity.getPos().getZ() + random.nextDouble(), 0, 0, 0);
			}

			if(Coord4D.get(tileEntity).sideVisible(EnumFacing.UP, world))
			{
				world.spawnParticle("reddust", tileEntity.getPos().getX() + random.nextDouble(), tileEntity.getPos().getY() + 1.01, tileEntity.getPos().getZ() + random.nextDouble(), 0, 0, 0);
			}

			if(Coord4D.get(tileEntity).sideVisible(EnumFacing.NORTH, world))
			{
				world.spawnParticle("reddust", tileEntity.getPos().getX() + random.nextDouble(), tileEntity.getPos().getY() + random.nextDouble(), tileEntity.getPos().getZ() + -.01, 0, 0, 0);
			}

			if(Coord4D.get(tileEntity).sideVisible(EnumFacing.SOUTH, world))
			{
				world.spawnParticle("reddust", tileEntity.getPos().getX() + random.nextDouble(), tileEntity.getPos().getY() + random.nextDouble(), tileEntity.getPos().getZ() + 1.01, 0, 0, 0);
			}

			if(Coord4D.get(tileEntity).sideVisible(EnumFacing.WEST, world))
			{
				world.spawnParticle("reddust", tileEntity.getPos().getX() + -.01, tileEntity.getPos().getY() + random.nextDouble(), tileEntity.getPos().getZ() + random.nextDouble(), 0, 0, 0);
			}

			if(Coord4D.get(tileEntity).sideVisible(EnumFacing.EAST, world))
			{
				world.spawnParticle("reddust", tileEntity.getPos().getX() + 1.01, tileEntity.getPos().getY() + random.nextDouble(), tileEntity.getPos().getZ() + random.nextDouble(), 0, 0, 0);
			}
		}

		iteratedNodes.add(tileEntity);

		for(EnumFacing side : EnumFacing.VALID_DIRECTIONS)
		{
			TileEntity tile = Coord4D.get(tileEntity).getFromSide(side).getTileEntity(pointer.getWorldObj());

			if(MultiblockManager.areEqual(tile, pointer) && !iteratedNodes.contains(tile))
			{
				loop((TileEntityMultiblock<?>)tile);
			}
		}
	}
}
