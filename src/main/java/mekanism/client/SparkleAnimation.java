package mekanism.client;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SparkleAnimation
{
	public TileEntity pointer;

	public Random random = new Random();

	public Set<TileEntity> iteratedNodes = new HashSet<TileEntity>();
	
	public INodeChecker nodeChecker;

	public SparkleAnimation(TileEntity tileEntity, INodeChecker checker)
	{
		pointer = tileEntity;
		nodeChecker = checker;
	}

	public void run()
	{
		try {
			if(general.dynamicTankEasterEgg)
			{
				pointer.getWorldObj().playSound(pointer.xCoord, pointer.yCoord, pointer.zCoord, "mekanism:etc.cj", 1F, 1F, false);
			}

			loop(pointer);
		} catch(Exception e) {}
	}

	public void loop(TileEntity tileEntity)
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
			Coord4D coord = Coord4D.get(tileEntity).getFromSide(side);
			
			if(coord.exists(pointer.getWorldObj()))
			{
				TileEntity tile = coord.getTileEntity(pointer.getWorldObj());
	
				if(tile != null && isNode(tile) && !iteratedNodes.contains(tile))
				{
					loop(tile);
				}
			}
		}
	}
	
	public boolean isNode(TileEntity tile)
	{
		return nodeChecker.isNode(tile);
	}
	
	public static interface INodeChecker
	{
		public boolean isNode(TileEntity tile);
	}
}
