package mekanism.common.tests.util;

import mekanism.api.functions.ConstantPredicates;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public class GameTestUtils {

    public static final int INACCESSIBLE_LEVEL = ChunkMap.MAX_VIEW_DISTANCE + 1;
    private static final int UNLOAD_LEVEL = ChunkLevel.MAX_LEVEL + 1;

    static final boolean DEBUG_CHUNK_LOADING = false;

    public static void succeedIfAfterUnload(ExtendedGameTestHelper helper, ChunkPos relativePos, Runnable criteria) {
        helper.startSequence()
              .thenWaitUntil(() -> unloadChunk(helper, relativePos))
              //Wait 5 ticks in case anything needs more time to process after the chunk unloads
              .thenIdle(5)
              .thenWaitUntil(0, criteria)
              .thenSucceed();
    }

    public static void succeedIfAfterReload(ExtendedGameTestHelper helper, ChunkPos relativePos, Runnable afterReload) {
        MutableInt lastLevel = new MutableInt(UNLOAD_LEVEL);
        helper.startSequence()
              .thenWaitUntil(() -> unloadChunk(helper, relativePos, lastLevel))
              //Wait 5 ticks in case anything needs more time to process after the chunk unloads
              .thenIdle(5)
              .thenWaitUntil(() -> loadChunk(helper, relativePos, lastLevel))
              //Wait 5 ticks in case anything needs more time to process after the chunk loads
              .thenIdle(5)
              .thenWaitUntil(0, afterReload)
              .thenSucceed();
    }

    //TODO - GameTest: Can we make unloads not cause the game to crash if a player tries to run a test that uses them instead of using game test server?
    // Most likely the answer is no
    private static void unloadChunk(ExtendedGameTestHelper helper, ChunkPos relativePos) {
        unloadChunk(helper, relativePos, new MutableInt());
    }

    private static void unloadChunk(ExtendedGameTestHelper helper, ChunkPos relativePos, MutableInt levelMemory) {
        ChunkPos absolutePos = absolutePos(helper, relativePos);
        long absPos = absolutePos.toLong();
        if (GameTestEventListeners.watchedChunks.containsKey(absPos)) {
            //Watched chunk
            if (GameTestEventListeners.watchedChunks.get(absPos)) {
                //Is loaded, but we are watching, throw exception so that we keep waiting for it to unload
                fail(helper, "Chunk has not been marked as unloaded yet", absolutePos, relativePos);
            } else {
                //Remove the watch on the chunk now that it has been unloaded
                GameTestEventListeners.watchedChunks.remove(absPos);
            }
        } else if (WorldUtils.isChunkLoaded(helper.getLevel(), absolutePos)) {
            //If the chunk isn't watched and is loaded we want to try and unload it
            ChunkMap chunkMap = helper.getLevel().getChunkSource().chunkMap;
            DistanceManager distanceManager = chunkMap.getDistanceManager();
            ChunkHolder holder = distanceManager.getChunk(absPos);
            //Watch the chunk and mark whether it is currently loaded or not
            if (holder != null) {
                //If it is loaded then we need to try and unload it
                GameTestEventListeners.watchedChunks.put(absPos, true);
                if (DEBUG_CHUNK_LOADING) {
                    Mekanism.logger.info("Trying to unload chunk at: {}, {}", absolutePos.x, absolutePos.z);
                }
                //If it is currently loaded, queue it for unload
                levelMemory.setValue(holder.getTicketLevel());
                distanceManager.updateChunkScheduling(absPos, UNLOAD_LEVEL, holder, holder.getTicketLevel());
                //And then unload it
                chunkMap.processUnloads(ConstantPredicates.ALWAYS_TRUE);
                fail(helper, "Chunk queued for unloading", absolutePos, relativePos);
            } else if (DEBUG_CHUNK_LOADING) {
                //Note: Even with debug logging enabled odds are this case isn't even possible due to the earlier check to skip if unloaded
                Mekanism.logger.info("Trying to unload already unloaded chunk at: {}, {}", absolutePos.x, absolutePos.z);
            }
        } else if (DEBUG_CHUNK_LOADING) {
            Mekanism.logger.info("Chunk at: {}, {} is already unloaded", absolutePos.x, absolutePos.z);
        }
    }

    private static void loadChunk(ExtendedGameTestHelper helper, ChunkPos relativePos) {
        loadChunk(helper, relativePos, new MutableInt(ChunkMap.FORCED_TICKET_LEVEL));
    }

    private static void loadChunk(ExtendedGameTestHelper helper, ChunkPos relativePos, MutableInt levelMemory) {
        ChunkPos absolutePos = absolutePos(helper, relativePos);
        long absPos = absolutePos.toLong();
        if (GameTestEventListeners.watchedChunks.containsKey(absPos)) {
            //Watched chunk
            if (GameTestEventListeners.watchedChunks.get(absPos)) {
                //Remove the watch on the chunk now that it has been loaded
                GameTestEventListeners.watchedChunks.remove(absPos);
            } else {
                //Not loaded, but we are watching, throw exception so that we keep waiting for it to load
                fail(helper, "Chunk has not been marked as loaded yet", absolutePos, relativePos);
            }
        } else if (!WorldUtils.isChunkLoaded(helper.getLevel(), absolutePos)) {
            //If the chunk isn't watched and is not loaded we want to try and load it
            ChunkMap chunkMap = helper.getLevel().getChunkSource().chunkMap;
            DistanceManager distanceManager = chunkMap.getDistanceManager();
            ChunkHolder holder = distanceManager.getChunk(absPos);
            if (holder == null) {
                //If it is currently unloaded, queue it for loading
                GameTestEventListeners.watchedChunks.put(absPos, false);
                if (DEBUG_CHUNK_LOADING) {
                    Mekanism.logger.info("Trying to load chunk at: {}, {}", absolutePos.x, absolutePos.z);
                }
                //Load the chunk to the level it was unloaded at
                holder = distanceManager.updateChunkScheduling(absPos, levelMemory.getValue(), null, UNLOAD_LEVEL);
                if (holder == null) {//Should never happen unless start value was unloaded
                    fail(helper, "Error loading chunk", absolutePos, relativePos);
                } else {
                    //And ensure we schedule it based on the status (in general this should be ChunkStatus.FULL)
                    chunkMap.schedule(holder, ChunkLevel.generationStatus(holder.getTicketLevel()));
                }
                fail(helper, "Chunk queued for loading", absolutePos, relativePos);
            } else if (DEBUG_CHUNK_LOADING) {
                //Note: Even with debug logging enabled odds are this case isn't even possible due to the earlier check to skip if loaded
                Mekanism.logger.info("Trying to load already loaded chunk at: {}, {}", absolutePos.x, absolutePos.z);
            }
        } else if (DEBUG_CHUNK_LOADING) {
            Mekanism.logger.info("Chunk at: {}, {} is already loaded", absolutePos.x, absolutePos.z);
        }
    }

    public static int setChunkLoadLevel(ExtendedGameTestHelper helper, ChunkPos relativePos, int newLevel) {
        ChunkPos absolutePos = absolutePos(helper, relativePos);
        long absPos = absolutePos.toLong();
        ChunkMap chunkMap = helper.getLevel().getChunkSource().chunkMap;
        DistanceManager distanceManager = chunkMap.getDistanceManager();
        ChunkHolder holder = distanceManager.getChunk(absPos);
        int oldLevel = holder == null ? UNLOAD_LEVEL : holder.getTicketLevel();
        distanceManager.updateChunkScheduling(absPos, newLevel, holder, oldLevel);
        //Note: We purposely don't unload or load it if that changed as this method is meant for use when we don't know if
        // we are loading or unloading, and instead want to simulate the weird inaccessible but not unloaded state that
        // vanilla can get chunks into
        return oldLevel;
    }

    public static ChunkPos absolutePos(ExtendedGameTestHelper helper, ChunkPos relativePos) {
        BlockPos relativeMiddle = relativePos.getMiddleBlockPosition(0);
        BlockPos absolutePos = helper.absolutePos(relativeMiddle);
        return new ChunkPos(absolutePos);
    }

    @Nullable
    public static BlockEntity getBlockEntity(ExtendedGameTestHelper helper, BlockPos relativePos) {
        return WorldUtils.getTileEntity(helper.getLevel(), helper.absolutePos(relativePos));
    }

    @Nullable
    public static <T extends BlockEntity> T getBlockEntity(ExtendedGameTestHelper helper, Class<T> clazz, BlockPos relativePos) {
        return WorldUtils.getTileEntity(clazz, helper.getLevel(), helper.absolutePos(relativePos));
    }

    //TODO: Make a PR to Neo that adds an overload for `assertContainerContains` to maybe do something like this or at least allow specifying the expected count
    public static void validateContainerHas(ExtendedGameTestHelper helper, BlockPos relativePos, int slot, ItemStack stack) {
        IItemHandler handler = helper.requireCapability(Capabilities.ITEM.block(), relativePos, null);
        ItemStack stored = handler.getStackInSlot(slot);
        if (!ItemStack.matches(stack, stored)) {
            if (stored.isEmpty()) {
                helper.fail("Slot " + slot + " in container should contain " + stack.getCount() + " " + stack.getItem() + ", but is empty", relativePos);
            } else {
                helper.fail("Slot " + slot + " in container should contain " + stack.getCount() + " " + stack.getItem() +
                            ". But instead contains " + stored.getCount() + " " + stored.getItem(), relativePos);
            }
        }
    }

    public static void fail(ExtendedGameTestHelper helper, String message, ChunkPos relativePos) {
        fail(helper, message, absolutePos(helper, relativePos), relativePos);
    }

    public static void fail(ExtendedGameTestHelper helper, String message, ChunkPos absolutePos, ChunkPos relativePos) {
        helper.fail(message + " at " + absolutePos.x + "," + absolutePos.z + " (relative: " + relativePos.x + "," + relativePos.z + ")");
    }
}