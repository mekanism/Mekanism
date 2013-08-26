package mekanism.client;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.Object3D;
import mekanism.common.Mekanism;
import mekanism.common.tileentity.TileEntityDynamicTank;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

@SideOnly(Side.CLIENT)
public class ThreadTankSparkle extends Thread
{
	public TileEntityDynamicTank pointer;
	
	public Random random = new Random();
	
	public Set<TileEntity> iteratedNodes = new HashSet<TileEntity>();
	
	public ThreadTankSparkle(TileEntityDynamicTank tileEntity)
	{
		pointer = tileEntity;
	}
	
	@Override
	public void run()
	{
		try {
			if(Mekanism.dynamicTankEasterEgg)
			{
				Mekanism.audioHandler.quickPlay("cj/CJ_" + (random.nextInt(3)+1) + ".ogg", pointer.worldObj, Object3D.get(pointer));
			}
			
			loop(pointer);
		} catch(Exception e) {}
	}
	
	public void loop(TileEntityDynamicTank tileEntity)
	{
		World world = pointer.worldObj;
		
		for(int i = 0; i < 6; i++)
		{
			if(Object3D.get(tileEntity).sideVisible(ForgeDirection.DOWN, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + random.nextDouble(), tileEntity.yCoord + -.01, tileEntity.zCoord + random.nextDouble(), 0, 0, 0);
			}
			
			if(Object3D.get(tileEntity).sideVisible(ForgeDirection.UP, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + random.nextDouble(), tileEntity.yCoord + 1.01, tileEntity.zCoord + random.nextDouble(), 0, 0, 0);
			}
			
			if(Object3D.get(tileEntity).sideVisible(ForgeDirection.NORTH, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + random.nextDouble(), tileEntity.yCoord + random.nextDouble(), tileEntity.zCoord + -.01, 0, 0, 0);
			}
			
			if(Object3D.get(tileEntity).sideVisible(ForgeDirection.SOUTH, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + random.nextDouble(), tileEntity.yCoord + random.nextDouble(), tileEntity.zCoord + 1.01, 0, 0, 0);
			}
			
			if(Object3D.get(tileEntity).sideVisible(ForgeDirection.WEST, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + -.01, tileEntity.yCoord + random.nextDouble(), tileEntity.zCoord + random.nextDouble(), 0, 0, 0);
			}
			
			if(Object3D.get(tileEntity).sideVisible(ForgeDirection.EAST, world))
			{
				world.spawnParticle("reddust", tileEntity.xCoord + 1.01, tileEntity.yCoord + random.nextDouble(), tileEntity.zCoord + random.nextDouble(), 0, 0, 0);
			}
		}
		
		iteratedNodes.add(tileEntity);
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = Object3D.get(tileEntity).getFromSide(side).getTileEntity(pointer.worldObj);
			
			if(tile instanceof TileEntityDynamicTank && !iteratedNodes.contains(tile))
			{
				loop((TileEntityDynamicTank)tile);
			}
		}
	}
}
