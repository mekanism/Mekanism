package mekanism.common.content.miner;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.util.BlockInfo;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

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
			int x = coord.getX()+i%diameter;
			int z = coord.getZ()+(i/diameter)%diameter;
			int y = coord.getY()+(i/diameter/diameter);

			BlockPos pos = new BlockPos(x, y, z);

			if(tileEntity.isInvalid())
			{
				return;
			}

			if(tileEntity.getPos().getX() == x && tileEntity.getPos().getY() == y && tileEntity.getPos().getZ() == z)
			{
				continue;
			}

			if(!tileEntity.getWorld().getChunkProvider().chunkExists(x >> 4, z >> 4))
			{
				continue;
			}

			TileEntity tile = tileEntity.getWorld().getTileEntity(pos);
			
			if(tile instanceof TileEntityBoundingBlock)
			{
				continue;
			}

			IBlockState state = tileEntity.getWorld().getBlockState(pos);
			info.block = state.getBlock();
			info.meta = info.block.getMetaFromState(state);

			if(info.block != null && !tileEntity.getWorld().isAirBlock(pos) && info.block.getBlockHardness(tileEntity.getWorld(), pos) >= 0)
			{
				MinerFilter filterFound = null;
				boolean canFilter;

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
					set(i, new Coord4D(x, y, z, tileEntity.getWorld().provider.getDimensionId()));
					replaceMap.put(i, filterFound);
					
					found++;
				}
			}
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
