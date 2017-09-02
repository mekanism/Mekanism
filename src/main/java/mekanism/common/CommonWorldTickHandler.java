package mekanism.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import mekanism.common.frequency.FrequencyManager;
import mekanism.common.multiblock.MultiblockManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class CommonWorldTickHandler
{
	private static final long maximumDeltaTimeNanoSecs = 16000000; // 16 milliseconds
	
	private HashMap<Integer, Queue<ChunkPos>> chunkRegenMap;
	
	public void addRegenChunk(int dimensionId, ChunkPos chunkCoord) 
	{
		if(chunkRegenMap == null) 
		{
			chunkRegenMap = new HashMap<>();
		}

		if(!chunkRegenMap.containsKey(dimensionId))
		{
			LinkedList<ChunkPos> list = new LinkedList<>();
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
			
			int dimensionId = world.provider.getDimension();

			//Credit to E. Beef
			if(chunkRegenMap.containsKey(dimensionId)) 
			{
				Queue<ChunkPos> chunksToGen = chunkRegenMap.get(dimensionId);
				long startTime = System.nanoTime();
				
				while(System.nanoTime() - startTime < maximumDeltaTimeNanoSecs && !chunksToGen.isEmpty()) 
				{
					ChunkPos nextChunk = chunksToGen.poll();
					
					if(nextChunk == null) 
					{ 
						break; 
					}

			        Random fmlRandom = new Random(world.getSeed());
			        long xSeed = fmlRandom.nextLong() >> 2 + 1L;
			        long zSeed = fmlRandom.nextLong() >> 2 + 1L;
			        fmlRandom.setSeed((xSeed*nextChunk.x + zSeed*nextChunk.z) ^ world.getSeed());

					Mekanism.genHandler.generate(fmlRandom, nextChunk.x, nextChunk.z, world, ((ChunkProviderServer)world.getChunkProvider()).chunkGenerator, world.getChunkProvider());
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
