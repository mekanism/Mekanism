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

	public Random random = new org.bogdang.modifications.random.XSTR();

	public Set<Coord4D> iteratedNodes = new HashSet<Coord4D>();
	
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
		
		try {
			new Thread() {
				@Override
				public void run()
				{
					World world = pointer.getWorldObj();
					
					for(Coord4D coord : iteratedNodes)
					{
						for(int i = 0; i < 6; i++)
						{
							world.spawnParticle("reddust", coord.xCoord + random.nextDouble(), coord.yCoord + -.01, coord.zCoord + random.nextDouble(), 0, 0, 0);
							world.spawnParticle("reddust", coord.xCoord + random.nextDouble(), coord.yCoord + 1.01, coord.zCoord + random.nextDouble(), 0, 0, 0);
							world.spawnParticle("reddust", coord.xCoord + random.nextDouble(), coord.yCoord + random.nextDouble(), coord.zCoord + -.01, 0, 0, 0);
							world.spawnParticle("reddust", coord.xCoord + random.nextDouble(), coord.yCoord + random.nextDouble(), coord.zCoord + 1.01, 0, 0, 0);
							world.spawnParticle("reddust", coord.xCoord + -.01, coord.yCoord + random.nextDouble(), coord.zCoord + random.nextDouble(), 0, 0, 0);
							world.spawnParticle("reddust", coord.xCoord + 1.01, coord.yCoord + random.nextDouble(), coord.zCoord + random.nextDouble(), 0, 0, 0);
						}
					}
				}
			}.start();
		} catch(Exception e) {}
	}

	public void loop(TileEntity tileEntity)
	{
		iteratedNodes.add(Coord4D.get(tileEntity));

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			Coord4D coord = Coord4D.get(tileEntity).getFromSide(side);
			
			if(coord.exists(pointer.getWorldObj()))
			{
				TileEntity tile = coord.getTileEntity(pointer.getWorldObj());
	
				if(tile != null && isNode(tile) && !iteratedNodes.contains(coord))
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
