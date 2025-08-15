package mekanism.common.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.common.Mekanism;
import mekanism.common.lib.multiblock.IInternalMultiblock;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldUtils {

    /**
     * Checks if the chunk at the given position is loaded but does not validate the position is in bounds of the world.
     *
     * @param world world
     * @param pos   position
     *
     * @see #isBlockLoaded(BlockGetter, BlockPos)
     */
    @Contract("null, _ -> false")
    public static boolean isChunkLoaded(@Nullable LevelReader world, @NotNull BlockPos pos) {
        return isChunkLoaded(world, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    /**
     * Checks if the chunk at the given position is loaded but does not validate the position is in bounds of the world.
     *
     * @param world world
     * @param pos   packed block position
     *
     * @see #isBlockLoaded(BlockGetter, BlockPos)
     */
    @Contract("null, _ -> false")
    public static boolean isChunkLoaded(@Nullable LevelReader world, long pos) {
        return isChunkLoaded(world, SectionPos.blockToSectionCoord(BlockPos.getX(pos)), SectionPos.blockToSectionCoord(BlockPos.getZ(pos)));
    }

    /**
     * Checks if the chunk at the given position is loaded.
     *
     * @param world    world
     * @param chunkPos Chunk position
     */
    @Contract("null, _ -> false")
    public static boolean isChunkLoaded(@Nullable LevelReader world, ChunkPos chunkPos) {
        return isChunkLoaded(world, chunkPos.x, chunkPos.z);
    }

    /**
     * Checks if the chunk at the given position is loaded.
     *
     * @param world  world
     * @param chunkX Chunk X coordinate
     * @param chunkZ Chunk Z coordinate
     */
    @Contract("null, _, _ -> false")
    public static boolean isChunkLoaded(@Nullable LevelReader world, int chunkX, int chunkZ) {
        if (world == null) {
            return false;
        } else if (world instanceof LevelAccessor accessor) {
            if (!(accessor instanceof Level level) || !level.isClientSide) {
                return accessor.hasChunk(chunkX, chunkZ);
            }
            //Don't allow the client level to just return true for all cases, as we actually care if it is present
            // and instead use the fallback logic that we have
        }
        return world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) != null;
    }

    /**
     * Checks if a position is in bounds of the world, and is loaded
     *
     * @param world world
     * @param pos   position
     *
     * @return True if the position is loaded or the given world is of a superclass of IWorldReader that does not have a concept of being loaded.
     */
    @Contract("null, _ -> false")
    public static boolean isBlockLoaded(@Nullable BlockGetter world, @NotNull BlockPos pos) {
        if (world == null) {
            return false;
        } else if (world instanceof LevelReader reader) {
            if (reader instanceof Level level && !level.isInWorldBounds(pos)) {
                return false;
            }
            //TODO: If any cases come up where things are behaving oddly due to the change from reader.hasChunkAt(pos)
            // re-evaluate this and if the specific case is being handled properly
            return isChunkLoaded(reader, pos);
        }
        return true;
    }

    private static boolean isInWorldBound(BlockGetter world, long pos) {
        return !world.isOutsideBuildHeight(BlockPos.getY(pos)) && isInWorldBoundsHorizontal(pos);
    }

    private static boolean isInWorldBoundsHorizontal(long pos) {
        int x = BlockPos.getX(pos);
        int z = BlockPos.getZ(pos);
        return x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000;
    }

    /**
     * Checks if a position is in bounds of the world, and is loaded
     *
     * @param world world
     * @param pos   packed block position
     *
     * @return True if the position is loaded or the given world is of a superclass of IWorldReader that does not have a concept of being loaded.
     *
     * @implNote the checks for world bound don't account for a mod changing the default 30,000,000 limit, but surely that's rare...
     */
    @Contract("null, _ -> false")
    public static boolean isBlockLoaded(@Nullable BlockGetter world, long pos) {
        if (world == null) {
            return false;
        } else if (world instanceof LevelReader reader) {
            if (reader instanceof Level level && !isInWorldBound(level, pos)) {
                return false;
            }
            //TODO: If any cases come up where things are behaving oddly due to the change from reader.hasChunkAt(pos)
            // re-evaluate this and if the specific case is being handled properly
            return isChunkLoaded(reader, pos);
        }
        return true;
    }

    /**
     * Checks if a position is in bounds of the world
     *
     * @param world world
     * @param pos   position
     *
     * @return True if the position is in bounds of the world or the given world is of a superclass of IWorldReader that does not have a concept of bounds.
     */
    @Contract("null, _ -> false")
    public static boolean isBlockInBounds(@Nullable BlockGetter world, @NotNull BlockPos pos) {
        if (world == null) {
            return false;
        } else if (world instanceof LevelReader reader) {
            return !(reader instanceof Level level) || level.isInWorldBounds(pos);
        }
        return true;
    }

    /**
     * Gets the chunk in a given position or {@code null} if there is no world, the position is out of bounds or the chunk isn't loaded. Tries to retrieve it from our
     * cache and if it isn't found, tries to get it from the world and adds it to our cache.
     *
     * @param world    world
     * @param chunkMap cached chunk map
     * @param pos      position
     *
     * @return The chunk in a given position or {@code null} if there is no world, the position is out of bounds or the chunk isn't loaded
     */
    @Nullable
    @Contract("null, _, _ -> null")
    private static ChunkAccess getChunkForPos(@Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos) {
        if (!isBlockInBounds(world, pos)) {
            //Allow the world to be nullable to remove warnings when we are calling things from a place that world could be null
            // Also short circuit to check if the position is out of bounds before bothering to look up the chunk
            return null;
        }
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        long combinedChunk = ChunkPos.asLong(chunkX, chunkZ);
        //We get the chunk rather than the world, so we can cache the chunk improving the overall
        // performance for retrieving a bunch of chunks in the general vicinity
        ChunkAccess chunk = chunkMap.get(combinedChunk);
        if (chunk == null) {
            //Get the chunk but don't force load it
            chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
            if (chunk != null) {
                chunkMap.put(combinedChunk, chunk);
            }
        }
        return chunk;
    }

    /**
     * Gets a blockstate if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache to more quickly be able to look up chunks if we are doing lots of lookups at once (For example multiblock structure validation)
     *
     * @param world    world
     * @param chunkMap cached chunk map
     * @param pos      position
     *
     * @return optional containing the blockstate if found, empty optional if not loaded
     */
    @NotNull
    public static Optional<BlockState> getBlockState(@Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos) {
        //Get the blockstate using the chunk we found/had cached
        return getBlockState(getChunkForPos(world, chunkMap, pos), pos);
    }

    /**
     * Gets a blockstate if the location is loaded
     *
     * @param world world
     * @param pos   position
     *
     * @return optional containing the blockstate if found, empty optional if not loaded
     */
    @NotNull
    public static Optional<BlockState> getBlockState(@Nullable BlockGetter world, @NotNull BlockPos pos) {
        if (!isBlockLoaded(world, pos)) {
            //If the world is null, or it is a world reader and the block is not loaded, return empty
            return Optional.empty();
        }
        return Optional.of(world.getBlockState(pos));
    }

    /**
     * Gets a blockstate if the location is loaded
     *
     * @param world world
     * @param pos   position
     *
     * @return the blockstate if found, null if not loaded
     */
    @Nullable
    public static BlockState getBlockStateIfLoaded(@Nullable BlockGetter world, @NotNull BlockPos pos) {
        if (!isBlockLoaded(world, pos)) {
            //If the world is null, or it is a world reader and the block is not loaded, return empty
            return null;
        }
        return world.getBlockState(pos);
    }

    /**
     * Gets a fluidstate if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache to more quickly be able to look up chunks if we are doing lots of lookups at once (For example multiblock structure validation)
     *
     * @param world    world
     * @param chunkMap cached chunk map
     * @param pos      position
     *
     * @return optional containing the fluidstate if found, empty optional if not loaded
     */
    @NotNull
    public static Optional<FluidState> getFluidState(@Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos) {
        //Get the fluidstate using the chunk we found/had cached
        return getFluidState(getChunkForPos(world, chunkMap, pos), pos);
    }

    /**
     * Gets a fluidstate if the location is loaded
     *
     * @param world world
     * @param pos   position
     *
     * @return optional containing the fluidstate if found, empty optional if not loaded
     */
    @NotNull
    public static Optional<FluidState> getFluidState(@Nullable BlockGetter world, @NotNull BlockPos pos) {
        if (!isBlockLoaded(world, pos)) {
            //If the world is null, or it is a world reader and the block is not loaded, return empty
            return Optional.empty();
        }
        return Optional.of(world.getFluidState(pos));
    }

    /**
     * Gets a tile entity if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache to more quickly be able to look up chunks if we are doing lots of lookups at once (For example the transporter pathfinding)
     *
     * @param world    world
     * @param chunkMap cached chunk map
     * @param pos      position
     *
     * @return tile entity if found, null if either not found or not loaded
     */
    @Nullable
    @Contract("null, _, _ -> null")
    public static BlockEntity getTileEntity(@Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos) {
        //Get the tile entity using the chunk we found/had cached
        return getTileEntity(getChunkForPos(world, chunkMap, pos), pos);
    }

    /**
     * Gets a tile entity if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache to more quickly be able to look up chunks if we are doing lots of lookups at once (For example the transporter pathfinding)
     *
     * @param clazz    Class type of the TileEntity we expect to be in the position
     * @param world    world
     * @param chunkMap cached chunk map
     * @param pos      position
     *
     * @return tile entity if found, null if either not found, not loaded, or of the wrong type
     */
    @Nullable
    @Contract("_, null, _, _ -> null")
    public static <T extends BlockEntity> T getTileEntity(@NotNull Class<T> clazz, @Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos) {
        return getTileEntity(clazz, world, chunkMap, pos, false);
    }

    /**
     * Gets a tile entity if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache to more quickly be able to look up chunks if we are doing lots of lookups at once (For example the transporter pathfinding)
     *
     * @param clazz        Class type of the TileEntity we expect to be in the position
     * @param world        world
     * @param chunkMap     cached chunk map
     * @param pos          position
     * @param logWrongType Whether an error should be logged if a tile of a different type is found at the position
     *
     * @return tile entity if found, null if either not found, not loaded, or of the wrong type
     */
    @Nullable
    @Contract("_, null, _, _, _ -> null")
    public static <T extends BlockEntity> T getTileEntity(@NotNull Class<T> clazz, @Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos,
          boolean logWrongType) {
        //Get the tile entity using the chunk we found/had cached
        return getTileEntity(clazz, getChunkForPos(world, chunkMap, pos), pos, logWrongType);
    }

    /**
     * Gets a tile entity if the location is loaded
     *
     * @param world world
     * @param pos   position
     *
     * @return tile entity if found, null if either not found or not loaded
     */
    @Nullable
    @Contract("null, _ -> null")
    public static BlockEntity getTileEntity(@Nullable BlockGetter world, long pos) {
        if (!isBlockLoaded(world, pos)) {
            //If the world is null, or it is a world reader and the block is not loaded, return null
            return null;
        }
        return world.getBlockEntity(BlockPos.of(pos));
    }

    /**
     * Gets a tile entity if the location is loaded
     *
     * @param world world
     * @param pos   position
     *
     * @return tile entity if found, null if either not found or not loaded
     */
    @Nullable
    @Contract("null, _ -> null")
    public static BlockEntity getTileEntity(@Nullable BlockGetter world, @NotNull BlockPos pos) {
        if (!isBlockLoaded(world, pos)) {
            //If the world is null, or it is a world reader and the block is not loaded, return null
            return null;
        }
        return world.getBlockEntity(pos);
    }

    /**
     * Gets the capability of a block at a given location if it is loaded
     *
     * @param level   Level
     * @param cap     Capability to look up
     * @param pos     position
     * @param context Capability context
     *
     * @return capability if present, null if either not found or not loaded
     */
    @Nullable
    @Contract("null, _, _, _ -> null")
    public static <CAP, CONTEXT> CAP getCapability(@Nullable Level level, BlockCapability<CAP, CONTEXT> cap, BlockPos pos, CONTEXT context) {
        return getCapability(level, cap, pos, null, null, context);
    }

    /**
     * Gets the capability of a block at a given location if it is loaded
     *
     * @param level   Level
     * @param cap     Capability to look up
     * @param pos     position
     * @param state   the block state, if known, or {@code null} if unknown
     * @param tile    the block entity, if known, or {@code null} if unknown
     * @param context Capability context
     *
     * @return capability if present, null if either not found or not loaded
     */
    @Nullable
    @Contract("null, _, _, _, _, _ -> null")
    public static <CAP, CONTEXT> CAP getCapability(@Nullable Level level, BlockCapability<CAP, CONTEXT> cap, BlockPos pos, @Nullable BlockState state,
          @Nullable BlockEntity tile, CONTEXT context) {
        if (!isBlockLoaded(level, pos)) {
            //If the world is null, or it is a world reader and the block is not loaded, return null
            return null;
        }
        return level.getCapability(cap, pos, state, tile, context);
    }

    /**
     * Gets a tile entity if the location is loaded
     *
     * @param clazz Class type of the TileEntity we expect to be in the position
     * @param world world
     * @param pos   position
     *
     * @return tile entity if found, null if either not found, not loaded, or of the wrong type
     */
    @Nullable
    @Contract("_, null, _ -> null")
    public static <T extends BlockEntity> T getTileEntity(@NotNull Class<T> clazz, @Nullable BlockGetter world, @NotNull BlockPos pos) {
        return getTileEntity(clazz, world, pos, false);
    }

    /**
     * Gets a tile entity if the location is loaded
     *
     * @param clazz        Class type of the TileEntity we expect to be in the position
     * @param world        world
     * @param pos          position
     * @param logWrongType Whether an error should be logged if a tile of a different type is found at the position
     *
     * @return tile entity if found, null if either not found or not loaded, or of the wrong type
     */
    @Nullable
    @Contract("_, null, _, _ -> null")
    public static <T extends BlockEntity> T getTileEntity(@NotNull Class<T> clazz, @Nullable BlockGetter world, @NotNull BlockPos pos, boolean logWrongType) {
        BlockEntity tile = getTileEntity(world, pos);
        if (tile == null) {
            return null;
        }
        if (clazz.isInstance(tile)) {
            return clazz.cast(tile);
        } else if (logWrongType) {
            Mekanism.logger.warn("Unexpected BlockEntity class at {}, expected {}, but found: {}", pos, clazz, tile.getClass());
        }
        return null;
    }

    /**
     * Marks a chunk as dirty if it is currently loaded
     */
    public static void markChunkDirty(Level world, BlockPos pos) {
        markChunkDirty(world, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    /**
     * Marks a chunk as dirty if it is currently loaded
     */
    public static void markChunkDirty(Level world, int chunkX, int chunkZ) {
        ChunkAccess chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        if (chunk != null) {
            chunk.setUnsaved(true);
        }
    }

    /**
     * Dismantles a block, adding to player inventory (or dropping it) and removing it from the world.
     */
    public static void dismantleBlock(BlockState state, Level world, BlockPos pos, @Nullable Entity entity, ItemStack tool) {
        dismantleBlock(state, world, pos, getTileEntity(world, pos), entity, tool);
    }

    /**
     * Dismantles a block, adding to player inventory (or dropping it) and removing it from the world.
     *
     * @implNote This method ignores {@link GameRules#RULE_DOBLOCKDROPS}, and does not drop experience.
     */
    public static void dismantleBlock(BlockState state, Level world, BlockPos pos, @Nullable BlockEntity tile, @Nullable Entity entity, ItemStack tool) {
        if (world instanceof ServerLevel level) {
            if (entity instanceof Player player) {
                for (ItemStack dropStack : getDrops(state, level, pos, tile, entity, tool)) {
                    if (player.addItem(dropStack)) {
                        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (world.random.nextFloat() - world.random.nextFloat()) * 1.4F + 2.0F);
                    } else {
                        player.drop(dropStack, false);
                    }
                }
            } else {
                for (ItemEntity drop : getDrops(state, level, pos, tile, entity, tool, true)) {
                    if (!drop.getItem().isEmpty()) {
                        level.addFreshEntity(drop);
                    }
                }
            }
            //Note: This will have no effect for any mek blocks currently
            state.spawnAfterBreak(level, pos, tool, false);
        }
        world.removeBlock(pos, false);
    }

    /**
     * Gets the drops from breaking the block at a given spot, including any drops added via the BlockDropsEvent
     */
    public static List<ItemEntity> getDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity tile, @Nullable Entity entity, ItemStack tool,
          boolean applyMomentum) {
        List<ItemStack> rawDrops = Block.getDrops(state, level, pos, tile, entity, tool);
        List<ItemEntity> initialDrops = new ArrayList<>(rawDrops.size());
        if (!rawDrops.isEmpty()) {
            double itemHeight = EntityType.ITEM.getHeight() / 2.0;
            for (ItemStack rawDrop : rawDrops) {
                if (!rawDrop.isEmpty()) {//Probably won't have empty stacks, but just in case
                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 0.5;
                    double z = pos.getZ() + 0.5;
                    if (applyMomentum) {
                        //Apply momentum similar to Block#popResource
                        x += Mth.nextDouble(level.random, -0.25, 0.25);
                        y += Mth.nextDouble(level.random, -0.25, 0.25) - itemHeight;
                        z += Mth.nextDouble(level.random, -0.25, 0.25);
                    }
                    ItemEntity item = new ItemEntity(level, x, y, z, rawDrop);
                    item.setDefaultPickUpDelay();
                    initialDrops.add(item);
                }
            }
        }
        BlockDropsEvent event = new BlockDropsEvent(level, pos, state, tile, initialDrops, entity, tool);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return Collections.emptyList();
        }
        return event.getDrops();
    }

    /**
     * Gets the drops from breaking the block at a given spot, including any drops added via the BlockDropsEvent
     */
    public static List<ItemStack> getDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity tile, @Nullable Entity entity, ItemStack tool) {
        List<ItemEntity> drops = getDrops(state, level, pos, tile, entity, tool, false);
        List<ItemStack> result = new ArrayList<>(drops.size());
        for (ItemEntity drop : drops) {
            ItemStack stack = drop.getItem();
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result;
    }

    /**
     * Gets the distance to a defined positions.
     *
     * @return the distance to the defined positions
     */
    public static double distanceBetween(BlockPos start, BlockPos end) {
        return Math.sqrt(start.distSqr(end));
    }

    /**
     * A method used to find the Direction represented by the distance of the defined Coord4D. Most likely won't have many applicable uses.
     *
     * @return Direction representing the side the defined relative Coord4D is on to this
     */
    public static Direction sideDifference(BlockPos pos, BlockPos other) {
        int xDiff = pos.getX() - other.getX();
        int yDiff = pos.getY() - other.getY();
        int zDiff = pos.getZ() - other.getZ();
        return getDirection(xDiff, yDiff, zDiff);
    }

    /**
     * A method used to find the Direction represented by the distance of the defined packed BlockPos. Most likely won't have many applicable uses.
     *
     * @return Direction representing the side the defined relative packed BlockPos is on to this
     */
    public static Direction sideDifference(long pos, long other) {
        int xDiff = BlockPos.getX(pos) - BlockPos.getX(other);
        int yDiff = BlockPos.getY(pos) - BlockPos.getY(other);
        int zDiff = BlockPos.getZ(pos) - BlockPos.getZ(other);
        return getDirection(xDiff, yDiff, zDiff);
    }

    private static @Nullable Direction getDirection(int xDiff, int yDiff, int zDiff) {
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (side.getStepX() == xDiff && side.getStepY() == yDiff && side.getStepZ() == zDiff) {
                return side;
            }
        }
        return null;
    }

    //TODO: Come up with a better method name, this is used for checking if a mob can spawn inside or if it is dangerous for mobs to say teleport inside of
    public static boolean isInsideFormedMultiblock(BlockGetter reader, BlockPos pos, @Nullable Mob mob) {
        BlockEntity tile = getTileEntity(reader, pos);
        if (tile instanceof IMultiblock<?> multiblockTile) {
            if (reader instanceof LevelReader levelReader && levelReader.isClientSide() || mob != null && mob.level().isClientSide()) {
                //If we are on the client just check if we are formed as we don't sync structure information
                // to the client. This way the client is at least relatively accurate with what values
                // it returns for if mobs can spawn
                return multiblockTile.getMultiblock().isFormed();
            }
            //If the multiblock is formed and the position above this block is inside the bounds of the multiblock
            // don't allow spawning on it.
            return multiblockTile.getMultiblock().isPositionInsideBounds(multiblockTile.getStructure(), pos.above());
        } else if (tile instanceof IStructuralMultiblock structuralMultiblock && structuralMultiblock.hasFormedMultiblock()) {
            //Note: This isn't actually used as all our structural multiblocks are transparent and vanilla tends to not let
            // mobs spawn on glass or stuff
            if (reader instanceof LevelReader levelReader && levelReader.isClientSide() || mob != null && mob.level().isClientSide()) {
                //If we are on the client return we can't spawn if it is formed. This way the client is at least relatively
                // accurate with what values it returns for if mobs can spawn
                return true;
            } else {
                BlockPos above = pos.above();
                for (Structure structure : structuralMultiblock.getStructureMap().values()) {
                    //Manually handle the getMultiblockData logic to avoid extra lookups
                    MultiblockData data = structure.getMultiblockData();
                    if (data != null && data.isFormed() && data.isPositionInsideBounds(structure, above)) {
                        //If the multiblock is formed and the position above this block is inside the bounds of the multiblock
                        // don't allow spawning on it.
                        return true;
                    }
                }
            }
            return false;
        }
        //If it is an internal multiblock don't allow spawning mobs on it if it is formed
        return tile instanceof IInternalMultiblock internalMultiblock && internalMultiblock.hasFormedMultiblock();
    }

    /**
     * Whether the provided chunk is being vibrated by a Seismic Vibrator.
     *
     * @param chunk chunk to check
     *
     * @return if the chunk is being vibrated
     */
    public static boolean isChunkVibrated(ChunkPos chunk, Level world) {
        for (GlobalPos coord : Mekanism.activeVibrators) {
            if (coord.dimension() == world.dimension() && SectionPos.blockToSectionCoord(coord.pos().getX()) == chunk.x &&
                SectionPos.blockToSectionCoord(coord.pos().getZ()) == chunk.z) {
                return true;
            }
        }
        return false;
    }

    public static boolean tryPlaceContainedLiquid(@Nullable Player player, Level world, BlockPos pos, @NotNull FluidStack fluidStack, @Nullable Direction side) {
        Fluid fluid = fluidStack.getFluid();
        FluidType fluidType = fluid.getFluidType();
        if (!fluidType.canBePlacedInLevel(world, pos, fluidStack)) {
            //If there is no fluid, or it cannot be placed in the world just
            return false;
        }
        BlockState state = world.getBlockState(pos);
        boolean isReplaceable = state.canBeReplaced(fluid);
        boolean canContainFluid = state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer && liquidBlockContainer.canPlaceLiquid(player, world, pos, state, fluid);
        if (state.isAir() || isReplaceable || canContainFluid) {
            if (fluidType.isVaporizedOnPlacement(world, pos, fluidStack)) {
                fluidType.onVaporize(player, world, pos, fluidStack);
            } else if (canContainFluid) {
                if (!((LiquidBlockContainer) state.getBlock()).placeLiquid(world, pos, state, fluidType.getStateForPlacement(world, pos, fluidStack))) {
                    //If something went wrong return that we couldn't actually place it
                    return false;
                }
                playEmptySound(player, world, pos, fluidType);
            } else {
                if (!world.isClientSide() && isReplaceable && !state.liquid()) {
                    world.destroyBlock(pos, true);
                }
                playEmptySound(player, world, pos, fluidType);
                world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
            }
            return true;
        }
        return side != null && tryPlaceContainedLiquid(player, world, pos.relative(side), fluidStack, null);
    }

    private static void playEmptySound(@Nullable Player player, LevelAccessor world, BlockPos pos, FluidType fluidType) {
        SoundEvent soundevent = fluidType.getSound(player, world, pos, SoundActions.BUCKET_EMPTY);
        if (soundevent != null) {
            world.playSound(player, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public static void playFillSound(@Nullable Player player, LevelAccessor world, BlockPos pos, @NotNull FluidStack fluidStack, @Nullable SoundEvent soundEvent) {
        if (soundEvent == null) {
            Fluid fluid = fluidStack.getFluid();
            Optional<SoundEvent> pickupSound = fluid.getPickupSound();
            //noinspection OptionalIsPresent - Capturing lambdas
            if (pickupSound.isPresent()) {
                soundEvent = pickupSound.get();
            } else {
                soundEvent = fluid.getFluidType().getSound(player, world, pos, SoundActions.BUCKET_FILL);
            }
        }
        if (soundEvent != null) {
            world.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    /**
     * Better version of the World.getRedstonePowerFromNeighbors() method that doesn't load chunks.
     *
     * @param world the world to perform the check in
     * @param pos   the position of the block performing the check
     *
     * @return if the block is indirectly getting powered by LOADED chunks
     */
    public static boolean isGettingPowered(Level world, BlockPos pos) {
        if (isBlockLoaded(world, pos)) {
            BlockPos.MutableBlockPos offset = new MutableBlockPos();
            for (Direction side : EnumUtils.DIRECTIONS) {
                offset.setWithOffset(pos, side);
                if (isBlockLoaded(world, offset)) {
                    BlockState blockState = world.getBlockState(offset);
                    boolean weakPower = blockState.getBlock().shouldCheckWeakPower(blockState, world, pos, side);
                    if (weakPower && isDirectlyGettingPowered(world, offset) || !weakPower && blockState.getSignal(world, offset, side) > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a block is directly getting powered by any of its neighbors without loading any chunks.
     *
     * @param world the world to perform the check in
     * @param pos   the BlockPos of the block to check
     *
     * @return if the block is directly getting powered
     */
    public static boolean isDirectlyGettingPowered(Level world, BlockPos pos) {
        BlockPos.MutableBlockPos offset = new MutableBlockPos();
        for (Direction side : EnumUtils.DIRECTIONS) {
            offset.setWithOffset(pos, side);
            if (isBlockLoaded(world, offset) && world.getSignal(pos, side) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all the positions are valid and the current block in them can be replaced.
     *
     * @return True if the blocks can be replaced and is within the world's bounds.
     */
    public static boolean areBlocksValidAndReplaceable(@NotNull BlockGetter world, @Nullable BlockPlaceContext baseContext, @NotNull BlockPos... positions) {
        for (BlockPos position : positions) {
            if (!isValidReplaceableBlock(world, baseContext, position)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all the positions are valid and the current block in them can be replaced.
     *
     * @return True if the blocks can be replaced and is within the world's bounds.
     */
    public static boolean areBlocksValidAndReplaceable(@NotNull BlockGetter world, @Nullable BlockPlaceContext baseContext, @NotNull Collection<BlockPos> positions) {
        //TODO: Potentially move more block placement over to these methods
        for (BlockPos position : positions) {
            if (!isValidReplaceableBlock(world, baseContext, position)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a block is valid for a position and the current block there can be replaced.
     *
     * @return True if the block can be replaced and is within the world's bounds.
     */
    public static boolean isValidReplaceableBlock(@NotNull BlockGetter world, @Nullable BlockPlaceContext baseContext, @NotNull BlockPos pos) {
        Optional<BlockState> blockState = getBlockState(world, pos);
        if (blockState.isPresent()) {
            BlockState state = blockState.get();
            if (baseContext == null) {
                return state.canBeReplaced();
            }
            return state.canBeReplaced(BlockPlaceContext.at(baseContext, pos, baseContext.getClickedFace()));
        }
        return false;
    }

    /**
     * Notifies neighboring blocks of a TileEntity change without loading chunks.
     *
     * @param world world to perform the operation in
     * @param pos   BlockPos to perform the operation on
     */
    public static void notifyLoadedNeighborsOfTileChange(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockPos.MutableBlockPos offset = new MutableBlockPos();
        for (Direction dir : EnumUtils.DIRECTIONS) {
            offset.setWithOffset(pos, dir);
            if (isBlockLoaded(world, offset)) {
                BlockState offsetState = world.getBlockState(offset);
                offsetState.onNeighborChange(world, offset, pos);
                offsetState.handleNeighborChanged(world, offset, state.getBlock(), pos, false);
                if (offsetState.isRedstoneConductor(world, offset)) {
                    //If redstone can be conducted through it, forward the change along an extra spot
                    offset.move(dir);
                    if (isBlockLoaded(world, offset)) {
                        offsetState = world.getBlockState(offset);
                        if (offsetState.getWeakChanges(world, offset)) {
                            offsetState.onNeighborChange(world, offset, pos);
                        }
                    }
                }
            }
        }
    }

    /**
     * Calls BOTH neighbour changed functions because nobody can decide on which one to implement, assuming that the neighboring position is loaded.
     *
     * @param world   world the change exists in
     * @param pos     neighbor to notify
     * @param fromPos pos of our block that updated
     */
    public static void notifyNeighborOfChange(@Nullable Level world, BlockPos pos, BlockPos fromPos) {
        Optional<BlockState> blockState = getBlockState(world, pos);
        if (blockState.isPresent() && world != null) {//World can't be null here but double check it
            BlockState state = blockState.get();
            state.onNeighborChange(world, pos, fromPos);
            state.handleNeighborChanged(world, pos, world.getBlockState(fromPos).getBlock(), fromPos, false);
        }
    }

    /**
     * Marks a block for a render update if loaded.
     *
     * @param world world the block is in
     * @param pos   Position of the block
     * @param state The block state at the position
     */
    public static void updateBlock(@Nullable Level world, @NotNull BlockPos pos, BlockState state) {
        if (isBlockLoaded(world, pos)) {
            world.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    /**
     * Vanilla copy of {@link net.minecraft.client.multiplayer.ClientLevel#getSkyDarken(float)} used to be World#getSunBrightness
     */
    public static float getSunBrightness(Level world, float partialTicks) {
        float f = world.getTimeOfDay(partialTicks);
        float f1 = 1.0F - (Mth.cos(f * Mth.TWO_PI) * 2.0F + 0.2F);
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        f1 = 1.0F - f1;
        f1 = (float) (f1 * (1.0D - world.getRainLevel(partialTicks) * 5.0F / 16.0D));
        f1 = (float) (f1 * (1.0D - world.getThunderLevel(partialTicks) * 5.0F / 16.0D));
        return f1 * 0.8F + 0.2F;
    }

    /**
     * Checks to see if the block at the position can see the sky, and it is daytime.
     *
     * @param world World to check in.
     * @param pos   Position to check.
     *
     * @return {@code true} if it can.
     */
    @Contract("null, _ -> false")
    public static boolean canSeeSun(@Nullable Level world, BlockPos pos) {
        //Note: We manually handle the world#isDaytime check by just checking the subtracted skylight
        // as vanilla returns false if the world's time is set to a fixed value even if that time
        // would effectively be daytime
        return world != null && world.dimensionType().hasSkyLight() && world.getSkyDarken() < 4 && world.canSeeSky(pos);
    }

    /**
     * Converts a long representing a {@link ChunkPos} to a {@link BlockPos} without creating a temporary {@link ChunkPos} object.
     *
     * @param chunkPos Pos to convert.
     */
    public static BlockPos getBlockPosFromChunkPos(long chunkPos) {
        return new BlockPos((int) chunkPos, 0, (int) (chunkPos >> 32));
    }

    /**
     * Get a packed block pos on the relative side.
     * @see BlockPos#relative(Direction)
     *
     * @param pos Source position
     * @param direction direction to offset in
     * @return the packed offset position
     */
    public static long relativePos(long pos, Direction direction) {
        return BlockPos.asLong(BlockPos.getX(pos) + direction.getStepX(), BlockPos.getY(pos) + direction.getStepY(), BlockPos.getZ(pos) + direction.getStepZ());
    }
}