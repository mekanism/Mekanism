package mekanism.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.lib.chunkloading.ChunkManager;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.world.GenHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonWorldTickHandler {

    private static final long maximumDeltaTimeNanoSecs = 16_000_000; // 16 milliseconds

    private Map<ResourceLocation, Queue<ChunkPos>> chunkRegenMap;
    public static boolean flushTagAndRecipeCaches;

    public void addRegenChunk(RegistryKey<World> dimension, ChunkPos chunkCoord) {
        if (chunkRegenMap == null) {
            chunkRegenMap = new Object2ObjectArrayMap<>();
        }
        ResourceLocation dimensionName = dimension.location();
        if (!chunkRegenMap.containsKey(dimensionName)) {
            LinkedList<ChunkPos> list = new LinkedList<>();
            list.add(chunkCoord);
            chunkRegenMap.put(dimensionName, list);
        } else {
            Queue<ChunkPos> regenPositions = chunkRegenMap.get(dimensionName);
            if (!regenPositions.contains(chunkCoord)) {
                regenPositions.add(chunkCoord);
            }
        }
    }

    public void resetRegenChunks() {
        if (chunkRegenMap != null) {
            chunkRegenMap.clear();
        }
    }

    @SubscribeEvent
    public void worldLoadEvent(WorldEvent.Load event) {
        if (!event.getWorld().isClientSide()) {
            FrequencyManager.load();
            RadiationManager.INSTANCE.createOrLoad();
            if (event.getWorld() instanceof ServerWorld) {
                ChunkManager.worldLoad((ServerWorld) event.getWorld());
            }
        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.END) {
            serverTick();
        }
    }

    @SubscribeEvent
    public void onTick(WorldTickEvent event) {
        if (event.side.isServer() && event.phase == Phase.END) {
            tickEnd((ServerWorld) event.world);
        }
    }

    private void serverTick() {
        FrequencyManager.tick();
        RadiationManager.INSTANCE.tickServer();
    }

    private void tickEnd(ServerWorld world) {
        if (!world.isClientSide) {
            RadiationManager.INSTANCE.tickServerWorld(world);
            if (flushTagAndRecipeCaches) {
                //Loop all open containers and if it is a portable qio dashboard force refresh the window's recipes
                for (ServerPlayerEntity player : world.players()) {
                    if (player.containerMenu instanceof PortableQIODashboardContainer) {
                        PortableQIODashboardContainer qioDashboard = (PortableQIODashboardContainer) player.containerMenu;
                        for (byte index = 0; index < IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS; index++) {
                            qioDashboard.getCraftingWindow(index).invalidateRecipe();
                        }
                    }
                }
                flushTagAndRecipeCaches = false;
            }

            if (chunkRegenMap == null || !MekanismConfig.world.enableRegeneration.get()) {
                return;
            }
            ResourceLocation dimensionName = world.dimension().location();
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
                    if (GenHandler.generate(world, fmlRandom, nextChunk.x, nextChunk.z)) {
                        Mekanism.logger.info("Regenerating ores at chunk {}", nextChunk);
                    }
                }
                if (chunksToGen.isEmpty()) {
                    chunkRegenMap.remove(dimensionName);
                }
            }
        }
    }
}