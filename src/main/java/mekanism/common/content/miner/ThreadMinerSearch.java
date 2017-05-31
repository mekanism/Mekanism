package mekanism.common.content.miner;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.util.BlockInfo;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.IFluidBlock;

public class ThreadMinerSearch extends Thread
{
	public TileEntityDigitalMiner tileEntity;

	public State state = State.IDLE;

	public Map<Chunk3D, BitSet> oresToMine = new HashMap<Chunk3D, BitSet>();
	public Map<Integer, MinerFilter> replaceMap = new HashMap<Integer, MinerFilter>();

	public Map<BlockInfo, MinerFilter> acceptedItems = new HashMap<BlockInfo, MinerFilter>();

	public int found = 0;

	public ThreadMinerSearch(TileEntityDigitalMiner tile)
	{
		tileEntity = tile;
	}

	// *------*
	// Support functions for alternative miner operations
	private boolean isInsideSphere( int x, int y, int z, int radius )
	{
		return Math.pow( x, 2 ) + Math.pow( y, 2 ) + Math.pow( z, 2 ) - Math.pow( radius, 2 ) <= 0;
	}
	
	private boolean isSurface( int x, int y, int z, int radius )
	{
		int setCount = 0;
		int unsetCount = 0;
		
		if( isInsideSphere( x+1, y, z, radius) ) setCount++; else unsetCount++;
		if( isInsideSphere( x-1, y, z, radius) ) setCount++; else unsetCount++;

		if( isInsideSphere( x, y+1, z, radius) ) setCount++; else unsetCount++;
		if( isInsideSphere( x, y-1, z, radius) ) setCount++; else unsetCount++;

		if( isInsideSphere( x, y, z+1, radius) ) setCount++; else unsetCount++;
		if( isInsideSphere( x, y, z-1, radius) ) setCount++; else unsetCount++;

		return setCount > 0 && unsetCount > 0;
	}
	// *------*

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
		BlockInfo info = new BlockInfo(null, 0);

		for(int i = 0; i < size; i++)
		{
			int x = coord.xCoord+i%diameter;
			int z = coord.zCoord+(i/diameter)%diameter;
			int y = general.minerAltOperation ? coord.yCoord-(i/diameter/diameter) : coord.yCoord+(i/diameter/diameter);

			if(tileEntity.isInvalid())
			{
				return;
			}

			try {
				if( y < 0 )
				{
					// Sanity check - shouldn't be needed, but just in case
					// Skip blocks outside map bounds
					continue;
				}
				if(tileEntity.getPos().getX() == x && tileEntity.getPos().getY() == y && tileEntity.getPos().getZ() == z)
				{
					// Skip block containing miner
					continue;
				}
	
				if(tileEntity.getWorld().getChunkProvider().getLoadedChunk(x >> 4, z >> 4) == null)
				{
					// Skip ungenerated chunks
					continue;
				}
	
				TileEntity tile = tileEntity.getWorld().getTileEntity(new BlockPos(x, y, z));
				
				if(tile instanceof TileEntityBoundingBlock)
				{
					// Skip bounding blocks
					continue;
				}

				IBlockState state = tileEntity.getWorld().getBlockState(new BlockPos(x, y, z));
				info.block = state.getBlock();
				info.meta = state.getBlock().getMetaFromState(state);
	
				if(info.block instanceof BlockLiquid || info.block instanceof IFluidBlock)
				{
					// Skip fluid blocks
					continue;
				}

				// Perform checks related to alternative operations
				if( general.minerAltOperation )
				{
					if( !isInsideSphere( x - tileEntity.getPos().getX(), Math.max( y - tileEntity.getPos().getY(), 0 ), z - tileEntity.getPos().getZ(), diameter / 2 ) )
					{
						// Skip blocks outside operating boundaries
						continue;
					}
				}

				if(info.block != null && !tileEntity.getWorld().isAirBlock(new BlockPos(x, y, z)) && state.getBlockHardness(tileEntity.getWorld(), new BlockPos(x, y, z)) >= 0)
				{
					MinerFilter filterFound = null;
					boolean canFilter = false;
	
					if(acceptedItems.containsKey(info))
					{
						filterFound = acceptedItems.get(info);
					}
					else {
						ItemStack stack = new ItemStack(info.block, 1, info.meta);
	
						if(tileEntity.isReplaceStack(stack))
						{
							continue;
						}
	
						for(MinerFilter filter : tileEntity.filters)
						{
							if(filter.canFilter(stack))
							{
								filterFound = filter;
								break;
							}
						}
	
						acceptedItems.put(info, filterFound);
					}
					
					canFilter = tileEntity.inverse ? filterFound == null : filterFound != null;
	
					if(canFilter)
					{
						set(i, new Coord4D(x, y, z, tileEntity.getWorld().provider.getDimension()));
						replaceMap.put(i, filterFound);
						
						found++;
					}
				}
			} catch(Exception e) {}
		}

		state = State.FINISHED;
		tileEntity.oresToMine = oresToMine;
		tileEntity.replaceMap = replaceMap;
		MekanismUtils.saveChunk(tileEntity);
	}
	
	public void set(int i, Coord4D location)
	{
		Chunk3D chunk = new Chunk3D(location);
		
		if(oresToMine.get(chunk) == null)
		{
			oresToMine.put(chunk, new BitSet());
		}
		
		oresToMine.get(chunk).set(i);
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
