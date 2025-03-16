package mekanism.common.tests.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.util.WorldUtils;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@NothingNullByDefault
public class MekGameTestHelper extends ExtendedGameTestHelper {

    public static final int INACCESSIBLE_LEVEL = ChunkMap.MAX_VIEW_DISTANCE + 1;
    public static final int UNLOAD_LEVEL = ChunkLevel.MAX_LEVEL + 1;

    public MekGameTestHelper(GameTestInfo info) {
        super(info);
    }

    public static HashedItem hashedStack(Item item) {
        return HashedItem.raw(new ItemStack(item));
    }

    public ChunkMap getChunkMap() {
        return getLevel().getChunkSource().chunkMap;
    }

    public int setChunkLoadLevel(ChunkPos relativePos, int newLevel) {
        long absPos = absolutePos(relativePos).toLong();
        DistanceManager distanceManager = getChunkMap().getDistanceManager();
        ChunkHolder holder = distanceManager.getChunk(absPos);
        int oldLevel = holder == null ? UNLOAD_LEVEL : holder.getTicketLevel();
        distanceManager.updateChunkScheduling(absPos, newLevel, holder, oldLevel);
        //Note: We purposely don't unload or load it if that changed as this method is meant for use when we don't know if
        // we are loading or unloading, and instead want to simulate the weird inaccessible but not unloaded state that
        // vanilla can get chunks into
        return oldLevel;
    }

    public ChunkPos absolutePos(ChunkPos relativePos) {
        BlockPos relativeMiddle = relativePos.getMiddleBlockPosition(0);
        BlockPos absolutePos = absolutePos(relativeMiddle);
        return new ChunkPos(absolutePos);
    }

    public BlockState getBlockState(int x, int y, int z) {
        return getBlockState(new BlockPos(x, y, z));
    }

    public boolean isChunkLoaded(ChunkPos relativePos) {
        return WorldUtils.isChunkLoaded(getLevel(), absolutePos(relativePos));
    }

    public boolean isBlockLoaded(BlockPos relativePos) {
        return WorldUtils.isBlockLoaded(getLevel(), absolutePos(relativePos));
    }

    public void fail(String message, ChunkPos relativePos) {
        ChunkPos absolutePos = absolutePos(relativePos);
        fail(message + " at " + absolutePos.x + "," + absolutePos.z + " (relative: " + relativePos.x + "," + relativePos.z + ") (t=" + getTick() + ")");
    }

    @Override
    public void assertContainerContains(BlockPos relativePos, Item item) {
        assertContainerContains(relativePos, item, 1);
    }

    public void assertContainerContains(int x, int y, int z, Item item, int count) {
        assertContainerContains(new BlockPos(x, y, z), item, count);
    }

    /**
     * This is similar and based off of vanilla's assertContainerContains, except supports checking for a specific amount, and checking blocks that expose item handlers.
     */
    public void assertContainerContains(BlockPos relativePos, Item item, int count) {
        //TODO: Do we want to make a PR to Neo that adds this overload, even if it is as simple as only checking the count
        // and doesn't also add support for checking item handlers?
        BlockEntity blockentity = getBlockEntity(relativePos);
        boolean sameCount;
        if (blockentity instanceof BaseContainerBlockEntity containerBE) {
            sameCount = containerBE.countItem(item) == count;
        } else {
            IItemHandler handler = getCapability(Capabilities.ITEM.block(), relativePos, null);
            if (handler == null) {
                throw new GameTestAssertException("Expected a container or item handler at " + relativePos + ", found " +
                                                  Util.getRegisteredName(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockentity.getType()));
            }
            int found = 0;
            for (int i = 0, slots = handler.getSlots(); i < slots; i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.is(item)) {
                    found += stack.getCount();
                }
            }
            sameCount = found == count;
        }
        if (!sameCount) {
            throw new GameTestAssertException("Container should contain: " + count + " " + item);
        }
    }

    /**
     * Adds support for validating that item handlers are empty.
     */
    @Override
    public void assertContainerEmpty(BlockPos relativePos) {
        BlockEntity blockentity = getBlockEntity(relativePos);
        if (blockentity instanceof BaseContainerBlockEntity containerBE) {
            if (!containerBE.isEmpty()) {
                throw new GameTestAssertException("Container should be empty");
            }
        } else {
            IItemHandler handler = getCapability(Capabilities.ITEM.block(), relativePos, null);
            if (handler != null) {
                for (int i = 0, slots = handler.getSlots(); i < slots; i++) {
                    if (!handler.getStackInSlot(i).isEmpty()) {
                        throw new GameTestAssertException("Container should be empty");
                    }
                }
            }
        }
    }

    public Player makeMockPlayerLookingAt(int x, int y, int z, Direction direction) {
        return makeMockPlayerLookingAt(new BlockPos(x, y, z), direction);
    }

    public Player makeMockPlayerLookingAt(BlockPos relativePos, Direction direction) {
        Player player = makeMockPlayer();
        BlockPos targetPos = absolutePos(relativePos);
        player.setPos(Vec3.upFromBottomCenterOf(targetPos.relative(direction.getOpposite()), -player.getEyeHeight()));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, targetPos.getCenter());
        return player;
    }

    /**
     * Adds support for providing a more accurate/useful Vec3 location in the hit result.
     */
    @Override
    public void useOn(BlockPos relativePos, ItemStack item, Player player, Direction direction) {
        useOn(relativePos, item, player, direction, 1);
    }

    public void useOn(BlockPos relativePos, ItemStack item, Player player, Direction direction, int times) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        BlockHitResult hit = createHitResult(relativePos, direction, false);
        UseOnContext context = new UseOnContext(getLevel(), player, InteractionHand.MAIN_HAND, item, hit);
        for (int i = 0; i < times; i++) {
            item.useOn(context);
        }
    }

    /**
     * Adds support for providing a more accurate/useful Vec3 location in the hit result.
     */
    @Override
    public void useBlock(BlockPos relativePos, Player player, ItemStack item, Direction direction) {
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        BlockHitResult hit = createHitResult(relativePos, direction, true);
        ItemInteractionResult result = getBlockState(relativePos).useItemOn(item, getLevel(), player, InteractionHand.MAIN_HAND, hit);
        if (!result.consumesAction()) {
            item.useOn(new UseOnContext(getLevel(), player, InteractionHand.MAIN_HAND, item, hit));
        }
    }

    //TODO: Do we want to PR the more accurate hit result location stuff to Neo?
    private BlockHitResult createHitResult(BlockPos relativePos, Direction direction, boolean inside) {
        BlockPos absolutePos = absolutePos(relativePos);
        return new BlockHitResult(absolutePos.getCenter().relative(direction, 0.5), direction, absolutePos, inside);
    }

    public <TYPE> TYPE cycleSerialization(Codec<TYPE> codec, TYPE sourceObject, Function<String, String> rawJsonReplacer) {
        RegistryOps<JsonElement> serializationContext = getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        DataResult<JsonElement> encodedResult = codec.encodeStart(serializationContext, sourceObject);
        //Note: Theoretically encoding shouldn't have any issues, but if it does, throw them
        JsonElement encoded = encodedResult.getOrThrow(GameTestAssertException::new);

        String asString = GsonHelper.toStableString(encoded);
        String replacedString = rawJsonReplacer.apply(asString);
        if (asString.equals(replacedString)) {
            fail("Could not find target to replace in: " + asString);
        }
        JsonElement toDecode = switch (encoded) {
            case JsonObject json -> GsonHelper.parse(replacedString);
            case JsonArray json -> GsonHelper.parseArray(replacedString);
            case null, default -> throw new GameTestAssertException("Unable to determine type of json element to decode");
        };

        DataResult<TYPE> decoded = codec.parse(serializationContext, toDecode);
        return decoded.getOrThrow(GameTestAssertException::new);
    }
}