package mekanism.client;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.common.tile.TileEntityMultiblock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThreadTankSparkle extends Thread
{
	public TileEntityMultiblock<?> pointer;

	public Random random = new Random();

	public Set<TileEntity> iteratedNodes = new HashSet<TileEntity>();

	public ThreadTankSparkle(TileEntityMultiblock<?> tileEntity)
	{
		pointer = tileEntity;
	}

	@Override
	public void run()
	{
		try {
			if(general.dynamicTankEasterEgg)
			{
				MekanismClient.audioHandler.quickPlay("cj/CJ_" + (random.nextInt(8)+1) + ".ogg", pointer.getWorldObj(), Coord4D.get(pointer));
			}

			loop(pointer);
		} catch(Exception e) {}
	}

	public void loop(TileEntityMultiblock<?> tileEntity)
	{
		World world = pointer.getWorldObj();

		for(int i = 0; i < 6; i++)
		{
			if(Coord4D.get(tileEntity).sideVisible(ForgeDirection.DOWN, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + random.nextDouble(), tileEntity.yCoord + -.01, tileEntity.zCoord + random.nextDouble(), 0, 0, 0);
			}

			if(Coord4D.get(tileEntity).sideVisible(ForgeDirection.UP, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + random.nextDouble(), tileEntity.yCoord + 1.01, tileEntity.zCoord + random.nextDouble(), 0, 0, 0);
			}

			if(Coord4D.get(tileEntity).sideVisible(ForgeDirection.NORTH, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + random.nextDouble(), tileEntity.yCoord + random.nextDouble(), tileEntity.zCoord + -.01, 0, 0, 0);
			}

			if(Coord4D.get(tileEntity).sideVisible(ForgeDirection.SOUTH, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + random.nextDouble(), tileEntity.yCoord + random.nextDouble(), tileEntity.zCoord + 1.01, 0, 0, 0);
			}

			if(Coord4D.get(tileEntity).sideVisible(ForgeDirection.WEST, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + -.01, tileEntity.yCoord + random.nextDouble(), tileEntity.zCoord + random.nextDouble(), 0, 0, 0);
			}

			if(Coord4D.get(tileEntity).sideVisible(ForgeDirection.EAST, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + 1.01, tileEntity.yCoord + random.nextDouble(), tileEntity.zCoord + random.nextDouble(), 0, 0, 0);
			}
		}

		iteratedNodes.add(tileEntity);

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = Coord4D.get(tileEntity).getFromSide(side).getTileEntity(pointer.getWorldObj());

			if(tile instanceof TileEntityMultiblock && tile.getClass() == pointer.getClass() && !iteratedNodes.contains(tile))
			{
				loop((TileEntityMultiblock<?>)tile);
			}
		}
	}
}
