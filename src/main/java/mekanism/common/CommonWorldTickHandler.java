package mekanism.common;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;
import mekanism.api.SerializationConstants;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.WorldUtils;
import mekanism.common.world.GenHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

public class CommonWorldTickHandler {

    private static final long maximumDeltaTimeNanoSecs = 16_000_000; // 16 milliseconds

    //TODO: I believe this may be fine as is with just the load and save methods being synchronized
    // but there is a chance this is not the case in which case we should adjust how this is done
    private Map<ResourceLocation, Object2IntMap<ChunkPos>> chunkVersions;
    private Map<ResourceLocation, Queue<ChunkPos>> chunkRegenMap;
    public static boolean flushTagAndRecipeCaches;
    public static boolean monitoringCardboardBox;
    @Nullable
    public static Predicate<ItemStack> fallbackItemCollector;

    public void addRegenChunk(ResourceKey<Level> dimension, ChunkPos chunkCoord) {
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

    public void resetChunkData() {
        chunkRegenMap = null;
        chunkVersions = null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        //If we are in the middle of breaking a block using a cardboard box, cancel any items
        // that are dropped, we do this at highest priority to ensure we cancel it the same tick
        // before forge replaces items with custom item entities with a tick delay
        // We also cancel any experience orbs from spawning as things like the furnace will store
        // how much xp they have but also try to drop it on replace
        if (monitoringCardboardBox) {
            Entity entity = event.getEntity();
            if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
                entity.discard();
                event.setCanceled(true);
            }
        } else if (!event.getLevel().isClientSide && fallbackItemCollector != null && event.getEntity() instanceof ItemEntity entity && fallbackItemCollector.test(entity.getItem())) {
            //If we have a fallback item collector active and the entity that is being added is an item,
            // try to let our fallback collector handle the item and keep track of it instead of actually adding it to the world
            entity.discard();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        //Skip empty block, shouldn't be a null state but the BreakEvent still handles that as the empty block,
        // so we need to skip handling it that way, and check if the player has access to destroy it
        //Note: The level should always be an instance of Level based on what is passed to the constructor of BreakEvent,
        // but we instance check it just to be safe
        if (state != null && !state.isAir() && event.getLevel() instanceof Level level &&
            !IBlockSecurityUtils.INSTANCE.canAccess(event.getPlayer(), level, event.getPos())) {
            //If they don't because it is something that is locked, then cancel the event
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public synchronized void chunkSave(ChunkDataEvent.Save event) {
        LevelAccessor world = event.getLevel();
        if (!world.isClientSide() && world instanceof Level level) {
            int chunkVersion = MekanismConfig.world.userGenVersion.get();
            if (chunkVersions != null) {
                chunkVersion = chunkVersions.getOrDefault(level.dimension().location(), Object2IntMaps.emptyMap())
                      .getOrDefault(event.getChunk().getPos(), chunkVersion);
            }
            event.getData().putInt(SerializationConstants.WORLD_GEN_VERSION, chunkVersion);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public synchronized void onChunkDataLoad(ChunkDataEvent.Load event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            int version = event.getData().getInt(SerializationConstants.WORLD_GEN_VERSION);
            //When a chunk is loaded, if it has an older version than the latest one
            if (version < MekanismConfig.world.userGenVersion.get()) {
                //Track what version it has so that when we save it, if we haven't gotten a chance to update
                // the chunk yet, then we are able to properly save that we still will need to update it
                if (chunkVersions == null) {
                    chunkVersions = new Object2ObjectArrayMap<>();
                }
                ChunkPos chunkCoord = event.getChunk().getPos();
                ResourceKey<Level> dimension = level.dimension();
                chunkVersions.computeIfAbsent(dimension.location(), dim -> new Object2IntOpenHashMap<>())
                      .put(chunkCoord, version);
                if (MekanismConfig.world.enableRegeneration.get()) {
                    //If retrogen is enabled, then we also need to mark the chunk as needing retrogen
                    addRegenChunk(dimension, chunkCoord);
                }
            }
        }
    }

    @SubscribeEvent
    public void chunkUnloadEvent(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide() && chunkVersions != null) {
            //When a chunk unloads, free up the memory tracking what version it has
            chunkVersions.getOrDefault(level.dimension().location(), Object2IntMaps.emptyMap())
                  .removeInt(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void worldUnloadEvent(LevelEvent.Unload event) {
        LevelAccessor world = event.getLevel();
        if (!world.isClientSide() && world instanceof Level level && chunkVersions != null) {
            //When a world unloads, free up memory tracking the versions of the chunks in it
            chunkVersions.remove(level.dimension().location());
        }
    }

    @SubscribeEvent
    public void worldLoadEvent(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            FrequencyManager.load();
            MultiblockManager.createOrLoadAll();
            QIOGlobalItemLookup.INSTANCE.createOrLoad();
        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent.Post event) {
        boolean tickingNormally = event.getServer().tickRateManager().runsNormally();
        FrequencyManager.tick(tickingNormally);
    }

    @SubscribeEvent
    public void onTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel world) {
            RadiationManager.get().tickServerWorld(world);
            //Note: We flush the tag and recipe cache, and also perform retrogen, regardless of if the ticks are frozen or not
            if (flushTagAndRecipeCaches) {
                //Loop all open containers and if it is a portable qio dashboard force refresh the window's recipes
                for (ServerPlayer player : world.players()) {
                    if (player.containerMenu instanceof PortableQIODashboardContainer qioDashboard) {
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
                //Chunk versions may be null if retrogen is forced by command
                Object2IntMap<ChunkPos> dimensionChunkVersions = chunkVersions == null ? Object2IntMaps.emptyMap() : chunkVersions.getOrDefault(dimensionName, Object2IntMaps.emptyMap());
                long startTime = System.nanoTime();
                while (System.nanoTime() - startTime < maximumDeltaTimeNanoSecs && !chunksToGen.isEmpty()) {
                    ChunkPos nextChunk = chunksToGen.poll();
                    if (nextChunk == null) {
                        break;
                    }
                    //Ensure the chunk actually exists and is still loaded before trying to retrogen it
                    if (WorldUtils.isChunkLoaded(world, nextChunk)) {
                        if (GenHandler.generate(world, nextChunk)) {
                            Mekanism.logger.info("Regenerating ores and salt at chunk {}", nextChunk);
                        }
                        //Regardless of whether we were able to generate anything in the chunk, now that we have
                        // handled it, update the chunk version. We do this by removing tracking the chunk's
                        // version so that we can just default it to the latest version when saved and free up the
                        // memory as early as possible
                        if (chunkVersions != null) {
                            dimensionChunkVersions.removeInt(nextChunk);
                        }
                    }
                }
                if (chunksToGen.isEmpty()) {
                    chunkRegenMap.remove(dimensionName);
                }
            }
        }
    }
}