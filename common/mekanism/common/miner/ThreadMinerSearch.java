package mekanism.common.miner;

import java.util.BitSet;
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
		
		if(!tileEntity.inverse && tileEntity.filters.isEmpty())
		{
			state = State.FINISHED;
			return;
		}
		
		Coord4D coord = tileEntity.getStartingCoord();
		int diameter = tileEntity.getDiameter();
		int size = tileEntity.getTotalSize();
		
		System.out.println(diameter + " " + size);
		
		for(int i = 0; i < size; i++)
		{
			int x = coord.xCoord+i%diameter;
			int z = coord.zCoord+(i/diameter)%diameter;
			int y = coord.yCoord+(i/diameter/diameter);
			
			if(tileEntity.isInvalid())
			{
				return;
			}
			
			if(tileEntity.xCoord == x && tileEntity.yCoord == y && tileEntity.zCoord == z)
			{
				continue;
			}
			
			if(tileEntity.worldObj.getBlockTileEntity(x, y, z) instanceof IBoundingBlock)
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
					tileEntity.oresToMine.set(i);
				}
			}
		}
		
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
