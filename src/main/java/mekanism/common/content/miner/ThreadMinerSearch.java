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

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

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

			if(!tileEntity.getWorldObj().getChunkProvider().chunkExists(x >> 4, z >> 4))
			{
				continue;
			}

			TileEntity tile = tileEntity.getWorldObj().getTileEntity(x, y, z);
			
			if(tile instanceof TileEntityBoundingBlock)
			{
				continue;
			}

			info.block = tileEntity.getWorldObj().getBlock(x, y, z);
			info.meta = tileEntity.getWorldObj().getBlockMetadata(x, y, z);

			if(info.block != null && !tileEntity.getWorldObj().isAirBlock(x, y, z) && info.block.getBlockHardness(tileEntity.getWorldObj(), x, y, z) >= 0)
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
					set(i, new Coord4D(x, y, z, tileEntity.getWorldObj().provider.dimensionId));
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
