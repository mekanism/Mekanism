package mekanism.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import mekanism.common.frequency.FrequencyManager;
import mekanism.common.multiblock.MultiblockManager;
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
		if(event.side == Side.SERVER)
		{
			if(event.phase == Phase.START)
			{
				tickStart(event.world);
			}
			else if(event.phase == Phase.END)
			{
				tickEnd(event.world);
			}
		}
	}
	
	public void tickStart(World world)
	{
		if(!world.isRemote)
		{
			if(!FrequencyManager.loaded)
			{
				FrequencyManager.load(world);
			}
		}
	}

	public void tickEnd(World world)
	{
		if(!world.isRemote)
		{
			MultiblockManager.tick(world);
			FrequencyManager.tick(world);
			
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
