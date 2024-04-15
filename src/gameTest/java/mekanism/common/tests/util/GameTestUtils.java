package mekanism.common.tests.util;

import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

public class GameTestUtils {

    public static final int INACCESSIBLE_LEVEL = ChunkMap.MAX_VIEW_DISTANCE + 1;
    public static final int UNLOAD_LEVEL = ChunkLevel.MAX_LEVEL + 1;

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
}