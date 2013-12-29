package mekanism.common.miner;

import java.util.Collections;

import mekanism.api.Object3D;
import mekanism.common.IBoundingBlock;
import mekanism.common.tileentity.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;

public class ThreadMinerSearch extends Thread
{
	public TileEntityDigitalMiner tileEntity;
	
	public State state = State.IDLE;
	
	public ThreadMinerSearch(TileEntityDigitalMiner tile)
	{
		tileEntity = tile;
	}
	
	@Override
	public void run()
	{
		state = State.SEARCHING;
		
		if(tileEntity.filters.isEmpty())
		{
			state = State.FINISHED;
			return;
		}
		
		for(int y = tileEntity.maxY; y >= tileEntity.minY; y--)
		{
			for(int x = tileEntity.xCoord-tileEntity.radius; x <= tileEntity.xCoord+tileEntity.radius; x++)
			{
				for(int z = tileEntity.zCoord-tileEntity.radius; z <= tileEntity.zCoord+tileEntity.radius; z++)
				{
					if(!tileEntity.worldObj.getChunkProvider().chunkExists(x >> 4, z >> 4) || tileEntity.isInvalid())
					{
						return;
					}
					
					if(Object3D.get(tileEntity).equals(new Object3D(x, y, z, tileEntity.worldObj.provider.dimensionId)))
					{
						continue;
					}
					
					if(new Object3D(x, y, z).getTileEntity(tileEntity.worldObj) instanceof IBoundingBlock)
					{
						continue;
					}
					
					int blockID = tileEntity.worldObj.getBlockId(x, y, z);
					int meta = tileEntity.worldObj.getBlockMetadata(x, y, z);
					
					if(blockID != 0)
					{
						ItemStack stack = new ItemStack(blockID, 1, meta);
						
						if(tileEntity.replaceStack != null && tileEntity.replaceStack.isItemEqual(stack))
						{
							continue;
						}
						
						for(MinerFilter filter : tileEntity.filters)
						{
							if(filter.canFilter(stack))
							{
								tileEntity.oresToMine.add(new Object3D(x, y, z));
							}
						}
					}
				}
			}
		}
		
		Collections.shuffle(tileEntity.oresToMine);
		
		state = State.FINISHED;
		MekanismUtils.saveChunk(tileEntity);
	}
	
	public void reset()
	{
		state = State.IDLE;
	}
	
	public static enum State
	{
		IDLE("Not ready"), 
		SEARCHING("Searching"), 
		PAUSED("Paused"),
		FINISHED("Ready");
		
		public String desc;
		
		private State(String s)
		{
			desc = s;
		}
	}
}
