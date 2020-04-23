package mekanism.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import mekanism.common.chunkloading.ChunkManager;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.world.GenHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    public void worldLoadEvent(WorldEvent.Load event) {
        if (!event.getWorld().isRemote()) {
            if (!FrequencyManager.loaded) {
                FrequencyManager.load();
            }
            if (event.getWorld() instanceof ServerWorld) {
                ChunkManager.worldLoad((ServerWorld) event.getWorld());
            }
        }
    }

    @SubscribeEvent
    public void onTick(WorldTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.END) {
            tickEnd(event.world);
        }
    }

    public void tickEnd(World world) {
        if (!world.isRemote) {
            MultiblockManager.tick(world);
            FrequencyManager.tick(world);
            ChunkManager.tick((ServerWorld) world);
            if (chunkRegenMap == null || !MekanismConfig.world.enableRegeneration.get()) {
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
                    if (GenHandler.generate((ServerWorld) world, fmlRandom, nextChunk.x, nextChunk.z)) {
                        Mekanism.logger.info("Regenerating ores at chunk " + nextChunk);
                    }
                }
                if (chunksToGen.isEmpty()) {
                    chunkRegenMap.remove(dimensionName);
                }
            }
        }
    }
}