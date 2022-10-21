package mekanism.common.util;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import mekanism.common.Mekanism;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkHolder.ChunkLoadingFailure;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
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
        } else if (world instanceof LevelAccessor accessor && accessor.getChunkSource() instanceof ServerChunkCache serverChunkCache) {
            CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> future = serverChunkCache.getChunkFuture(chunkX, chunkZ, ChunkStatus.FULL, false);
            return future.isDone() && future.getNow(ChunkHolder.UNLOADED_CHUNK).left().isPresent();
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
    public static BlockEntity getTileEntity(@Nullable BlockGetter world, @NotNull BlockPos pos) {
        if (!isBlockLoaded(world, pos)) {
            //If the world is null, or it is a world reader and the block is not loaded, return null
            return null;
        }
        return world.getBlockEntity(pos);
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
            Mekanism.logger.warn("Unexpected TileEntity class at {}, expected {}, but found: {}", pos, clazz, tile.getClass());
        }
        return null;
    }

    /**
     * Marks the chunk this TileEntity is in as modified. Call this method to be sure NBT is written by the defined tile entity.
     *
     * @param tile TileEntity to save
     */
    public static void saveChunk(BlockEntity tile) {
        if (tile != null && !tile.isRemoved() && tile.getLevel() != null) {
            markChunkDirty(tile.getLevel(), tile.getBlockPos());
        }
    }

    /**
     * Marks a chunk as dirty if it is currently loaded
     */
    public static void markChunkDirty(Level world, BlockPos pos) {
        if (isBlockLoaded(world, pos)) {
            world.getChunkAt(pos).setUnsaved(true);
        }
    }

    /**
     * Dismantles a block, dropping it and removing it from the world.
     */
    public static void dismantleBlock(BlockState state, Level world, BlockPos pos) {
        dismantleBlock(state, world, pos, getTileEntity(world, pos));
    }

    /**
     * Dismantles a block, dropping it and removing it from the world.
     */
    public static void dismantleBlock(BlockState state, Level world, BlockPos pos, @Nullable BlockEntity tile) {
        Block.dropResources(state, world, pos, tile);
        world.removeBlock(pos, false);
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
        BlockPos diff = pos.subtract(other);
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (side.getStepX() == diff.getX() && side.getStepY() == diff.getY() && side.getStepZ() == diff.getZ()) {
                return side;
            }
        }
        return null;
    }

    /**
     * Whether the provided chunk is being vibrated by a Seismic Vibrator.
     *
     * @param chunk chunk to check
     *
     * @return if the chunk is being vibrated
     */
    public static boolean isChunkVibrated(ChunkPos chunk, Level world) {
        return Mekanism.activeVibrators.stream().anyMatch(coord -> coord.dimension == world.dimension() && SectionPos.blockToSectionCoord(coord.getX()) == chunk.x &&
                                                                   SectionPos.blockToSectionCoord(coord.getZ()) == chunk.z);
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
        boolean canContainFluid = state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer && liquidBlockContainer.canPlaceLiquid(world, pos, state, fluid);
        if (state.isAir() || isReplaceable || canContainFluid) {
            if (world.dimensionType().ultraWarm() && fluidType.isVaporizedOnPlacement(world, pos, fluidStack)) {
                fluidType.onVaporize(player, world, pos, fluidStack);
            } else if (canContainFluid) {
                if (!((LiquidBlockContainer) state.getBlock()).placeLiquid(world, pos, state, fluidType.getStateForPlacement(world, pos, fluidStack))) {
                    //If something went wrong return that we couldn't actually place it
                    return false;
                }
                playEmptySound(player, world, pos, fluidType, fluidStack);
            } else {
                if (!world.isClientSide() && isReplaceable && !state.getMaterial().isLiquid()) {
                    world.destroyBlock(pos, true);
                }
                playEmptySound(player, world, pos, fluidType, fluidStack);
                world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
            }
            return true;
        }
        return side != null && tryPlaceContainedLiquid(player, world, pos.relative(side), fluidStack, null);
    }

    private static void playEmptySound(@Nullable Player player, LevelAccessor world, BlockPos pos, FluidType fluidType, @NotNull FluidStack fluidStack) {
        SoundEvent soundevent = fluidType.getSound(player, world, pos, SoundActions.BUCKET_EMPTY);
        if (soundevent == null) {
            soundevent = MekanismTags.Fluids.LAVA_LOOKUP.contains(fluidStack.getFluid()) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }
        world.playSound(player, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static void playFillSound(@Nullable Player player, LevelAccessor world, BlockPos pos, @NotNull FluidStack fluidStack, @Nullable SoundEvent soundEvent) {
        if (soundEvent == null) {
            Fluid fluid = fluidStack.getFluid();
            soundEvent = fluid.getPickupSound().orElseGet(() -> fluid.getFluidType().getSound(player, world, pos, SoundActions.BUCKET_FILL));
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
            for (Direction side : EnumUtils.DIRECTIONS) {
                BlockPos offset = pos.relative(side);
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
        for (Direction side : EnumUtils.DIRECTIONS) {
            BlockPos offset = pos.relative(side);
            if (isBlockLoaded(world, offset)) {
                if (world.getSignal(pos, side) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if all the positions are valid and the current block in them can be replaced.
     *
     * @return True if the blocks can be replaced and is within the world's bounds.
     */
    public static boolean areBlocksValidAndReplaceable(@NotNull BlockGetter world, @NotNull BlockPos... positions) {
        return areBlocksValidAndReplaceable(world, Arrays.stream(positions));
    }

    /**
     * Checks if all the positions are valid and the current block in them can be replaced.
     *
     * @return True if the blocks can be replaced and is within the world's bounds.
     */
    public static boolean areBlocksValidAndReplaceable(@NotNull BlockGetter world, @NotNull Collection<BlockPos> positions) {
        //TODO: Potentially move more block placement over to these methods
        return areBlocksValidAndReplaceable(world, positions.stream());
    }

    /**
     * Checks if all the positions are valid and the current block in them can be replaced.
     *
     * @return True if the blocks can be replaced and is within the world's bounds.
     */
    public static boolean areBlocksValidAndReplaceable(@NotNull BlockGetter world, @NotNull Stream<BlockPos> positions) {
        return positions.allMatch(pos -> isValidReplaceableBlock(world, pos));
    }

    /**
     * Checks if a block is valid for a position and the current block there can be replaced.
     *
     * @return True if the block can be replaced and is within the world's bounds.
     */
    public static boolean isValidReplaceableBlock(@NotNull BlockGetter world, @NotNull BlockPos pos) {
        return isBlockInBounds(world, pos) && world.getBlockState(pos).getMaterial().isReplaceable();
    }

    /**
     * Notifies neighboring blocks of a TileEntity change without loading chunks.
     *
     * @param world world to perform the operation in
     * @param pos   BlockPos to perform the operation on
     */
    public static void notifyLoadedNeighborsOfTileChange(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        for (Direction dir : EnumUtils.DIRECTIONS) {
            BlockPos offset = pos.relative(dir);
            if (isBlockLoaded(world, offset)) {
                notifyNeighborOfChange(world, offset, pos);
                if (world.getBlockState(offset).isRedstoneConductor(world, offset)) {
                    offset = offset.relative(dir);
                    if (isBlockLoaded(world, offset)) {
                        Block block1 = world.getBlockState(offset).getBlock();
                        //TODO: Make sure this is passing the correct state
                        if (block1.getWeakChanges(state, world, offset)) {
                            block1.onNeighborChange(state, world, offset, pos);
                        }
                    }
                }
            }
        }
    }

    /**
     * Calls BOTH neighbour changed functions because nobody can decide on which one to implement, assuming that the neighboring positions are loaded.
     *
     * @param world     world the change exists in
     * @param fromPos   pos of our block that updated
     * @param neighbors Sides to notify the neighbors on.
     */
    public static void notifyNeighborsOfChange(@Nullable Level world, BlockPos fromPos, Set<Direction> neighbors) {
        if (!neighbors.isEmpty()) {
            getBlockState(world, fromPos).ifPresent(sourceState -> {
                for (Direction neighbor : neighbors) {
                    BlockPos pos = fromPos.relative(neighbor);
                    getBlockState(world, pos).ifPresent(state -> {
                        state.onNeighborChange(world, pos, fromPos);
                        state.neighborChanged(world, pos, sourceState.getBlock(), fromPos, false);
                    });
                }
            });
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
        getBlockState(world, pos).ifPresent(state -> {
            state.onNeighborChange(world, pos, fromPos);
            state.neighborChanged(world, pos, world.getBlockState(fromPos).getBlock(), fromPos, false);
        });
    }

    /**
     * Calls BOTH neighbour changed functions because nobody can decide on which one to implement, assuming that the neighboring position is loaded.
     *
     * @param world        world the change exists in
     * @param neighborSide The side the neighbor to notify is on
     * @param fromPos      pos of our block that updated
     */
    public static void notifyNeighborOfChange(@Nullable Level world, Direction neighborSide, BlockPos fromPos) {
        notifyNeighborOfChange(world, fromPos.relative(neighborSide), fromPos);
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
     * Rechecks the lighting at a specific block's position if the block is loaded.
     *
     * @param world world the block is in
     * @param pos   coordinates
     */
    public static void recheckLighting(@Nullable BlockAndTintGetter world, @NotNull BlockPos pos) {
        if (isBlockLoaded(world, pos)) {
            world.getLightEngine().checkBlock(pos);
        }
    }

    /**
     * Vanilla copy of {@link net.minecraft.client.multiplayer.ClientLevel#getSkyDarken(float)} used to be World#getSunBrightness
     */
    public static float getSunBrightness(Level world, float partialTicks) {
        float f = world.getTimeOfDay(partialTicks);
        float f1 = 1.0F - (Mth.cos(f * ((float) Math.PI * 2F)) * 2.0F + 0.2F);
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
}