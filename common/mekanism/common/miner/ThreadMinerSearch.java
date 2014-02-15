package mekanism.common.miner;

import java.util.Collections;

import mekanism.api.Coord4D;
import mekanism.common.IBoundingBlock;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
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
					if(tileEntity.isInvalid())
					{
						return;
					}
					
					if(Coord4D.get(tileEntity).equals(new Coord4D(x, y, z, tileEntity.worldObj.provider.dimensionId)))
					{
						continue;
					}
					
					if(new Coord4D(x, y, z, tileEntity.worldObj.provider.dimensionId).getTileEntity(tileEntity.worldObj) instanceof IBoundingBlock)
					{
						continue;
					}
					
					int blockID = tileEntity.worldObj.getBlockId(x, y, z);
					int meta = tileEntity.worldObj.getBlockMetadata(x, y, z);
					
					if(blockID != 0 && blockID != Block.bedrock.blockID)
					{
						ItemStack stack = new ItemStack(blockID, 1, meta);
						
						if(tileEntity.replaceStack != null && tileEntity.replaceStack.isItemEqual(stack))
						{
							continue;
						}
						
						boolean hasFilter = false;
						
						for(MinerFilter filter : tileEntity.filters)
						{
							if(filter.canFilter(stack))
							{
								hasFilter = true;
							}
						}
						
						if(tileEntity.inverse ? !hasFilter : hasFilter)
						{
							tileEntity.oresToMine.add(new Coord4D(x, y, z, tileEntity.worldObj.provider.dimensionId));
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
