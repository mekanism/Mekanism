package mekanism.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.common.tank.DynamicTankCache;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;

public class CommonWorldTickHandler
{
	private static final long maximumDeltaTimeNanoSecs = 16000000; // 16 milliseconds
	
	private HashMap<Integer, Queue<ChunkCoordIntPair>> chunkRegenMap;
	
	public void addRegenChunk(int dimensionId, ChunkCoordIntPair chunkCoord) 
	{
		if(chunkRegenMap == null) 
		{
			chunkRegenMap = new HashMap<Integer, Queue<ChunkCoordIntPair>>();
		}

		if(!chunkRegenMap.containsKey(dimensionId))
		{
			LinkedList<ChunkCoordIntPair> list = new LinkedList<ChunkCoordIntPair>();
			list.add(chunkCoord);
			chunkRegenMap.put(dimensionId, list);
		}
		else {
			if(!chunkRegenMap.get(dimensionId).contains(chunkCoord)) 
			{
				chunkRegenMap.get(dimensionId).add(chunkCoord);
			}
		}
	}
	
	public void resetRegenChunks()
	{
		if(chunkRegenMap != null)
		{
			chunkRegenMap.clear();
		}
	}
	
	@SubscribeEvent
	public void onTick(WorldTickEvent event)
	{
		if(event.side == Side.SERVER && event.phase == Phase.END)
		{
			tickEnd(event.world);
		}
	}

	public void tickEnd(World world)
	{
		ArrayList<Integer> idsToKill = new ArrayList<Integer>();
		HashMap<Integer, HashSet<Coord4D>> tilesToKill = new HashMap<Integer, HashSet<Coord4D>>();

		if(!world.isRemote)
		{
			for(Map.Entry<Integer, DynamicTankCache> entry : Mekanism.dynamicInventories.entrySet())
			{
				int inventoryID = entry.getKey();

				for(Coord4D obj : entry.getValue().locations)
				{
					if(obj.dimensionId == world.provider.dimensionId)
					{
						TileEntity tileEntity = obj.getTileEntity(world);

						if(!(tileEntity instanceof TileEntityDynamicTank) || ((TileEntityDynamicTank)tileEntity).inventoryID != inventoryID)
						{
							if(!tilesToKill.containsKey(inventoryID))
							{
								tilesToKill.put(inventoryID, new HashSet<Coord4D>());
							}

							tilesToKill.get(inventoryID).add(obj);
						}
					}
				}

				if(entry.getValue().locations.isEmpty())
				{
					idsToKill.add(inventoryID);
				}
			}

			for(Map.Entry<Integer, HashSet<Coord4D>> entry : tilesToKill.entrySet())
			{
				for(Coord4D obj : entry.getValue())
				{
					Mekanism.dynamicInventories.get(entry.getKey()).locations.remove(obj);
				}
			}

			for(int inventoryID : idsToKill)
			{
				for(Coord4D obj : Mekanism.dynamicInventories.get(inventoryID).locations)
				{
					TileEntityDynamicTank dynamicTank = (TileEntityDynamicTank)obj.getTileEntity(world);

					if(dynamicTank != null)
					{
						dynamicTank.cachedData = new DynamicTankCache();
						dynamicTank.inventory = new ItemStack[2];
						dynamicTank.inventoryID = -1;
					}
				}

				Mekanism.dynamicInventories.remove(inventoryID);
			}
			
			if(chunkRegenMap == null) 
			{ 
				return; 
			}
			
			int dimensionId = world.provider.dimensionId;

			//Credit to E. Beef
			if(chunkRegenMap.containsKey(dimensionId)) 
			{
				Queue<ChunkCoordIntPair> chunksToGen = chunkRegenMap.get(dimensionId);
				long startTime = System.nanoTime();
				
				while(System.nanoTime() - startTime < maximumDeltaTimeNanoSecs && !chunksToGen.isEmpty()) 
				{
					ChunkCoordIntPair nextChunk = chunksToGen.poll();
					
					if(nextChunk == null) 
					{ 
						break; 
					}

			        Random fmlRandom = new Random(world.getSeed());
			        long xSeed = fmlRandom.nextLong() >> 2 + 1L;
			        long zSeed = fmlRandom.nextLong() >> 2 + 1L;
			        fmlRandom.setSeed((xSeed*nextChunk.chunkXPos + zSeed*nextChunk.chunkZPos) ^ world.getSeed());

					Mekanism.genHandler.generate(fmlRandom, nextChunk.chunkXPos, nextChunk.chunkZPos, world, world.getChunkProvider(), world.getChunkProvider());
					Mekanism.logger.info("[Mekanism] Regenerating ores at chunk " + nextChunk);
				}

				if(chunksToGen.isEmpty()) 
				{
					chunkRegenMap.remove(dimensionId);
				}
			}
		}
	}
}
