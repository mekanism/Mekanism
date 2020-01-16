package mekanism.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.world.GenHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class CommonWorldTickHandler {

    private static final long maximumDeltaTimeNanoSecs = 16_000_000; // 16 milliseconds

    private Map<ResourceLocation, Queue<ChunkPos>> chunkRegenMap;

    public void addRegenChunk(DimensionType dimension, ChunkPos chunkCoord) {
        if (chunkRegenMap == null) {
            chunkRegenMap = new Object2ObjectArrayMap<>();
        }
        ResourceLocation dimensionName = dimension.getRegistryName();
        if (!chunkRegenMap.containsKey(dimensionName)) {
            LinkedList<ChunkPos> list = new LinkedList<>();
            list.add(chunkCoord);
            chunkRegenMap.put(dimensionName, list);
        } else if (!chunkRegenMap.get(dimensionName).contains(chunkCoord)) {
            chunkRegenMap.get(dimensionName).add(chunkCoord);
        }
    }

    public void resetRegenChunks() {
        if (chunkRegenMap != null) {
            chunkRegenMap.clear();
        }
    }

    @SubscribeEvent
    public void onTick(WorldTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
            if (event.phase == Phase.START) {
                tickStart(event.world);
            } else if (event.phase == Phase.END) {
                tickEnd(event.world);
            }
        }
    }

    public void tickStart(World world) {
        if (!world.isRemote) {
            if (!FrequencyManager.loaded) {
                FrequencyManager.load(world);
            }
        }
    }

    public void tickEnd(World world) {
        if (!world.isRemote) {
            MultiblockManager.tick(world);
            FrequencyManager.tick(world);
            if (chunkRegenMap == null) {
                return;
            }
            ResourceLocation dimensionName = world.getDimension().getType().getRegistryName();
            //Credit to E. Beef
            if (chunkRegenMap.containsKey(dimensionName)) {
                Queue<ChunkPos> chunksToGen = chunkRegenMap.get(dimensionName);
                long startTime = System.nanoTime();
                while (System.nanoTime() - startTime < maximumDeltaTimeNanoSecs && !chunksToGen.isEmpty()) {
                    ChunkPos nextChunk = chunksToGen.poll();
                    if (nextChunk == null) {
                        break;
                    }

                    Random fmlRandom = new Random(world.getSeed());
                    long xSeed = fmlRandom.nextLong() >> 2 + 1L;
                    long zSeed = fmlRandom.nextLong() >> 2 + 1L;
                    fmlRandom.setSeed((xSeed * nextChunk.x + zSeed * nextChunk.z) ^ world.getSeed());
                    GenHandler.generate(world, ((ServerChunkProvider) world.getChunkProvider()).getChunkGenerator(), fmlRandom, nextChunk.x, nextChunk.z);
                    Mekanism.logger.info("Regenerating ores at chunk " + nextChunk);
                }
                if (chunksToGen.isEmpty()) {
                    chunkRegenMap.remove(dimensionName);
                }
            }
        }
    }
}