package mekanism.common.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.interfaces.IActiveState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

public class WorldUtils {

    /**
     * Checks if a position is in bounds of the world, and is loaded
     *
     * @param world world
     * @param pos   position
     *
     * @return True if the position is loaded or the given world is of a superclass of IWorldReader that does not have a concept of being loaded.
     */
    @Contract("null, _ -> false")
    public static boolean isBlockLoaded(@Nullable IBlockReader world, @Nonnull BlockPos pos) {
        if (world == null || !World.isValid(pos)) {
            return false;
        } else if (world instanceof IWorldReader) {
            //Note: We don't bother checking if it is a world and then isBlockPresent because
            // all that does is also validate the y value is in bounds, and we already check to make
            // sure the position is valid both in the y and xz directions
            return ((IWorldReader) world).isBlockLoaded(pos);
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
    private static IChunk getChunkForPos(@Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos) {
        if (world == null || !World.isValid(pos)) {
            //Allow the world to be nullable to remove warnings when we are calling things from a place that world could be null
            // Also short circuit to check if the position is out of bounds before bothering to lookup the chunk
            return null;
        }
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        long combinedChunk = (((long) chunkX) << 32) | (chunkZ & 0xFFFFFFFFL);
        //We get the chunk rather than the world so we can cache the chunk improving the overall
        // performance for retrieving a bunch of chunks in the general vicinity
        IChunk chunk = chunkMap.get(combinedChunk);
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
     * we found back in the cache so as to more quickly be able to lookup chunks if we are doing lots of lookups at once (For example multiblock structure validation)
     *
     * @param world    world
     * @param chunkMap cached chunk map
     * @param pos      position
     *
     * @return optional containing the blockstate if found, empty optional if not loaded
     */
    @Nonnull
    public static Optional<BlockState> getBlockState(@Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos) {
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
    @Nonnull
    public static Optional<BlockState> getBlockState(@Nullable IBlockReader world, @Nonnull BlockPos pos) {
        if (!isBlockLoaded(world, pos)) {
            //If the world is null or its a world reader and the block is not loaded, return empty
            return Optional.empty();
        }
        return Optional.of(world.getBlockState(pos));
    }

    /**
     * Gets a fluidstate if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache so as to more quickly be able to lookup chunks if we are doing lots of lookups at once (For example multiblock structure validation)
     *
     * @param world    world
     * @param chunkMap cached chunk map
     * @param pos      position
     *
     * @return optional containing the fluidstate if found, empty optional if not loaded
     */
    @Nonnull
    public static Optional<FluidState> getFluidState(@Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos) {
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
    @Nonnull
    public static Optional<FluidState> getFluidState(@Nullable IBlockReader world, @Nonnull BlockPos pos) {
        if (!isBlockLoaded(world, pos)) {
            //If the world is null or its a world reader and the block is not loaded, return empty
            return Optional.empty();
        }
        return Optional.of(world.getFluidState(pos));
    }

    /**
     * Gets a tile entity if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache so as to more quickly be able to lookup chunks if we are doing lots of lookups at once (For example the transporter pathfinding)
     *
     * @param world    world
     * @param chunkMap cached chunk map
     * @param pos      position
     *
     * @return tile entity if found, null if either not found or not loaded
     */
    @Nullable
    @Contract("null, _, _ -> null")
    public static TileEntity getTileEntity(@Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos) {
        //Get the tile entity using the chunk we found/had cached
        return getTileEntity(getChunkForPos(world, chunkMap, pos), pos);
    }

    /**
     * Gets a tile entity if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache so as to more quickly be able to lookup chunks if we are doing lots of lookups at once (For example the transporter pathfinding)
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
    public static <T extends TileEntity> T getTileEntity(@Nonnull Class<T> clazz, @Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos) {
        return getTileEntity(clazz, world, chunkMap, pos, false);
    }

    /**
     * Gets a tile entity if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache so as to more quickly be able to lookup chunks if we are doing lots of lookups at once (For example the transporter pathfinding)
     *
     * @param clazz        Class type of the TileEntity we expect to be in the position
     * @param world        world
     * @param chunkMap     cached chunk map
     * @param pos          position
     * @param logWrongType Whether or not an error should be logged if a tile of a different type is found at the position
     *
     * @return tile entity if found, null if either not found, not loaded, or of the wrong type
     */
    @Nullable
    @Contract("_, null, _, _, _ -> null")
    public static <T extends TileEntity> T getTileEntity(@Nonnull Class<T> clazz, @Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos,
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
    public static TileEntity getTileEntity(@Nullable IBlockReader world, @Nonnull BlockPos pos) {
        if (!isBlockLoaded(world, pos)) {
            //If the world is null or its a world reader and the block is not loaded, return null
            return null;
        }
        return world.getTileEntity(pos);
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
    public static <T extends TileEntity> T getTileEntity(@Nonnull Class<T> clazz, @Nullable IBlockReader world, @Nonnull BlockPos pos) {
        return getTileEntity(clazz, world, pos, false);
    }

    /**
     * Gets a tile entity if the location is loaded
     *
     * @param clazz        Class type of the TileEntity we expect to be in the position
     * @param world        world
     * @param pos          position
     * @param logWrongType Whether or not an error should be logged if a tile of a different type is found at the position
     *
     * @return tile entity if found, null if either not found or not loaded, or of the wrong type
     */
    @Nullable
    @Contract("_, null, _, _ -> null")
    public static <T extends TileEntity> T getTileEntity(@Nonnull Class<T> clazz, @Nullable IBlockReader world, @Nonnull BlockPos pos, boolean logWrongType) {
        TileEntity tile = getTileEntity(world, pos);
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
    public static void saveChunk(TileEntity tile) {
        if (tile != null && !tile.isRemoved() && tile.getWorld() != null) {
            markChunkDirty(tile.getWorld(), tile.getPos());
        }
    }

    /**
     * Marks a chunk as dirty if it is currently loaded
     */
    public static void markChunkDirty(World world, BlockPos pos) {
        if (isBlockLoaded(world, pos)) {
            world.getChunkAt(pos).markDirty();
        }
        //TODO: This line below is now (1.16+) called by the mark chunk dirty method (without even validating if it is loaded).
        // And with it causes issues where chunks are easily ghost loaded. Why was it added like that and do we need to somehow
        // also update neighboring comparators
        //world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock()); //Notify neighbors of changes
    }

    /**
     * Dismantles a block, dropping it and removing it from the world.
     */
    public static void dismantleBlock(BlockState state, World world, BlockPos pos) {
        dismantleBlock(state, world, pos, getTileEntity(world, pos));
    }

    /**
     * Dismantles a block, dropping it and removing it from the world.
     */
    public static void dismantleBlock(BlockState state, World world, BlockPos pos, @Nullable TileEntity tile) {
        Block.spawnDrops(state, world, pos, tile);
        world.removeBlock(pos, false);
    }

    /**
     * Gets the distance to a defined Coord4D.
     *
     * @return the distance to the defined Coord4D
     */
    public static double distanceBetween(BlockPos start, BlockPos end) {
        return MathHelper.sqrt(start.distanceSq(end));
    }

    /**
     * A method used to find the Direction represented by the distance of the defined Coord4D. Most likely won't have many applicable uses.
     *
     * @return Direction representing the side the defined relative Coord4D is on to this
     */
    public static Direction sideDifference(BlockPos pos, BlockPos other) {
        BlockPos diff = pos.subtract(other);
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (side.getXOffset() == diff.getX() && side.getYOffset() == diff.getY() && side.getZOffset() == diff.getZ()) {
                return side;
            }
        }
        return null;
    }

    /**
     * Whether or not the provided chunk is being vibrated by a Seismic Vibrator.
     *
     * @param chunk chunk to check
     *
     * @return if the chunk is being vibrated
     */
    public static boolean isChunkVibrated(ChunkPos chunk, World world) {
        return Mekanism.activeVibrators.stream().anyMatch(coord -> coord.dimension == world.getDimensionKey() && coord.getX() >> 4 == chunk.x && coord.getZ() >> 4 == chunk.z);
    }

    public static boolean tryPlaceContainedLiquid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nonnull FluidStack fluidStack, @Nullable Direction side) {
        Fluid fluid = fluidStack.getFluid();
        if (!fluid.getAttributes().canBePlacedInWorld(world, pos, fluidStack)) {
            //If there is no fluid or it cannot be placed in the world just
            return false;
        }
        BlockState state = world.getBlockState(pos);
        boolean isReplaceable = state.isReplaceable(fluid);
        boolean canContainFluid = state.getBlock() instanceof ILiquidContainer && ((ILiquidContainer) state.getBlock()).canContainFluid(world, pos, state, fluid);
        if (state.isAir(world, pos) || isReplaceable || canContainFluid) {
            if (world.getDimensionType().isUltrawarm() && fluid.getAttributes().doesVaporize(world, pos, fluidStack)) {
                fluid.getAttributes().vaporize(player, world, pos, fluidStack);
            } else if (canContainFluid) {
                if (!((ILiquidContainer) state.getBlock()).receiveFluid(world, pos, state, fluid.getAttributes().getStateForPlacement(world, pos, fluidStack))) {
                    //If something went wrong return that we couldn't actually place it
                    return false;
                }
                playEmptySound(player, world, pos, fluidStack);
            } else {
                if (!world.isRemote() && isReplaceable && !state.getMaterial().isLiquid()) {
                    world.destroyBlock(pos, true);
                }
                playEmptySound(player, world, pos, fluidStack);
                world.setBlockState(pos, fluid.getDefaultState().getBlockState(), BlockFlags.DEFAULT_AND_RERENDER);
            }
            return true;
        }
        return side != null && tryPlaceContainedLiquid(player, world, pos.offset(side), fluidStack, null);
    }

    private static void playEmptySound(@Nullable PlayerEntity player, IWorld world, BlockPos pos, @Nonnull FluidStack fluidStack) {
        SoundEvent soundevent = fluidStack.getFluid().getAttributes().getEmptySound(world, pos);
        if (soundevent == null) {
            soundevent = fluidStack.getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        }
        world.playSound(player, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public static void playFillSound(@Nullable PlayerEntity player, IWorld world, BlockPos pos, @Nonnull FluidStack fluidStack) {
        SoundEvent soundevent = fluidStack.getFluid().getAttributes().getFillSound(world, pos);
        if (soundevent == null) {
            soundevent = fluidStack.getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
        }
        world.playSound(player, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Better version of the World.getRedstonePowerFromNeighbors() method that doesn't load chunks.
     *
     * @param world the world to perform the check in
     * @param pos   the position of the block performing the check
     *
     * @return if the block is indirectly getting powered by LOADED chunks
     */
    public static boolean isGettingPowered(World world, BlockPos pos) {
        for (Direction side : EnumUtils.DIRECTIONS) {
            BlockPos offset = pos.offset(side);
            if (isBlockLoaded(world, pos) && isBlockLoaded(world, offset)) {
                BlockState blockState = world.getBlockState(offset);
                boolean weakPower = blockState.getBlock().shouldCheckWeakPower(blockState, world, pos, side);
                if (weakPower && isDirectlyGettingPowered(world, offset) || !weakPower && blockState.getWeakPower(world, offset, side) > 0) {
                    return true;
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
    public static boolean isDirectlyGettingPowered(World world, BlockPos pos) {
        for (Direction side : EnumUtils.DIRECTIONS) {
            BlockPos offset = pos.offset(side);
            if (isBlockLoaded(world, offset)) {
                if (world.getRedstonePower(pos, side) > 0) {
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
    public static boolean areBlocksValidAndReplaceable(@Nonnull IBlockReader world, @Nonnull BlockPos... positions) {
        return areBlocksValidAndReplaceable(world, Arrays.stream(positions));
    }

    /**
     * Checks if all the positions are valid and the current block in them can be replaced.
     *
     * @return True if the blocks can be replaced and is within the world's bounds.
     */
    public static boolean areBlocksValidAndReplaceable(@Nonnull IBlockReader world, @Nonnull Collection<BlockPos> positions) {
        //TODO: Potentially move more block placement over to these methods
        return areBlocksValidAndReplaceable(world, positions.stream());
    }

    /**
     * Checks if all the positions are valid and the current block in them can be replaced.
     *
     * @return True if the blocks can be replaced and is within the world's bounds.
     */
    public static boolean areBlocksValidAndReplaceable(@Nonnull IBlockReader world, @Nonnull Stream<BlockPos> positions) {
        return positions.allMatch(pos -> isValidReplaceableBlock(world, pos));
    }

    /**
     * Checks if a block is valid for a position and the current block there can be replaced.
     *
     * @return True if the block can be replaced and is within the world's bounds.
     */
    public static boolean isValidReplaceableBlock(@Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return World.isValid(pos) && world.getBlockState(pos).getMaterial().isReplaceable();
    }

    /**
     * Notifies neighboring blocks of a TileEntity change without loading chunks.
     *
     * @param world world to perform the operation in
     * @param pos   BlockPos to perform the operation on
     */
    public static void notifyLoadedNeighborsOfTileChange(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        for (Direction dir : EnumUtils.DIRECTIONS) {
            BlockPos offset = pos.offset(dir);
            if (isBlockLoaded(world, offset)) {
                notifyNeighborOfChange(world, offset, pos);
                if (world.getBlockState(offset).isNormalCube(world, offset)) {
                    offset = offset.offset(dir);
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
     * Calls BOTH neighbour changed functions because nobody can decide on which one to implement, assuming that the neighboring position is loaded.
     *
     * @param world   world the change exists in
     * @param pos     neighbor to notify
     * @param fromPos pos of our block that updated
     */
    public static void notifyNeighborOfChange(@Nullable World world, BlockPos pos, BlockPos fromPos) {
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
    public static void notifyNeighborOfChange(@Nullable World world, Direction neighborSide, BlockPos fromPos) {
        notifyNeighborOfChange(world, fromPos.offset(neighborSide), fromPos);
    }

    /**
     * Places a fake bounding block at the defined location.
     *
     * @param world            world to place block in
     * @param boundingLocation coordinates of bounding block
     * @param orig             original block position
     */
    public static void makeBoundingBlock(@Nullable IWorld world, BlockPos boundingLocation, BlockPos orig) {
        if (world == null) {
            return;
        }
        BlockBounding boundingBlock = MekanismBlocks.BOUNDING_BLOCK.getBlock();
        BlockState newState = BlockStateHelper.getStateForPlacement(boundingBlock, boundingBlock.getDefaultState(), world, boundingLocation, null, Direction.NORTH);
        world.setBlockState(boundingLocation, newState, BlockFlags.DEFAULT);
        if (!world.isRemote()) {
            TileEntityBoundingBlock tile = getTileEntity(TileEntityBoundingBlock.class, world, boundingLocation);
            if (tile != null) {
                tile.setMainLocation(orig);
            } else {
                Mekanism.logger.warn("Unable to find Bounding Block Tile at: {}", boundingLocation);
            }
        }
    }

    /**
     * Places a fake advanced bounding block at the defined location.
     *
     * @param world            world to place block in
     * @param boundingLocation coordinates of bounding block
     * @param orig             original block position
     */
    public static void makeAdvancedBoundingBlock(IWorld world, BlockPos boundingLocation, BlockPos orig) {
        BlockBounding boundingBlock = MekanismBlocks.ADVANCED_BOUNDING_BLOCK.getBlock();
        BlockState newState = BlockStateHelper.getStateForPlacement(boundingBlock, boundingBlock.getDefaultState(), world, boundingLocation, null, Direction.NORTH);
        world.setBlockState(boundingLocation, newState, BlockFlags.DEFAULT);
        if (!world.isRemote()) {
            TileEntityAdvancedBoundingBlock tile = getTileEntity(TileEntityAdvancedBoundingBlock.class, world, boundingLocation);
            if (tile != null) {
                tile.setMainLocation(orig);
            } else {
                Mekanism.logger.warn("Unable to find Advanced Bounding Block Tile at: {}", boundingLocation);
            }
        }
    }

    /**
     * Updates a block's light value and marks it for a render update.
     *
     * @param world world the block is in
     * @param pos   Position of the block
     * @param tile The tile entity at the position
     */
    public static void updateBlock(@Nullable World world, BlockPos pos, TileEntity tile) {
        if (!isBlockLoaded(world, pos)) {
            return;
        }
        //Schedule a render update regardless of it is an IActiveState with IActiveState#renderUpdate() as true
        // This is because that is mainly used for rendering machine effects, but we need to run a render update
        // anyways here in case IActiveState#renderUpdate() is false and we just had the block rotate.
        // For example the laser, or charge pad.
        //TODO: Render update
        //world.markBlockRangeForRenderUpdate(pos, pos);
        BlockState blockState = world.getBlockState(pos);
        //TODO: Fix this as it is not ideal to just pretend the block was previously air to force it to update
        // Maybe should use notifyUpdate
        world.markBlockRangeForRenderUpdate(pos, Blocks.AIR.getDefaultState(), blockState);
        if (!(tile instanceof IActiveState) || ((IActiveState) tile).lightUpdate() && MekanismConfig.client.machineEffects.get()) {
            //Update all light types at the position
            recheckLighting(world, pos);
        }
    }

    /**
     * Rechecks the lighting at a specific block's position
     *
     * @param world world the block is in
     * @param pos   coordinates
     */
    public static void recheckLighting(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos) {
        world.getLightManager().checkBlock(pos);
    }

    /**
     * Vanilla copy of {@link net.minecraft.client.world.ClientWorld#getSunBrightness(float)} used to be World#getSunBrightness
     */
    public static float getSunBrightness(World world, float partialTicks) {
        float f = world.func_242415_f(partialTicks);
        float f1 = 1.0F - (MathHelper.cos(f * ((float) Math.PI * 2F)) * 2.0F + 0.2F);
        f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
        f1 = 1.0F - f1;
        f1 = (float) (f1 * (1.0D - world.getRainStrength(partialTicks) * 5.0F / 16.0D));
        f1 = (float) (f1 * (1.0D - world.getThunderStrength(partialTicks) * 5.0F / 16.0D));
        return f1 * 0.8F + 0.2F;
    }

    /**
     * Checks to see if the block at the position can see the sky and it is daytime.
     *
     * @param world World to check in.
     * @param pos   Position to check.
     *
     * @return {@code true} if it can.
     */
    @Contract("null, _ -> false")
    public static boolean canSeeSun(@Nullable World world, BlockPos pos) {
        //Note: We manually handle the world#isDaytime check by just checking the subtracted skylight
        // as vanilla returns false if the world's time is set to a fixed value even if that time
        // would effectively be daytime
        return world != null && world.getDimensionType().hasSkyLight() && world.getSkylightSubtracted() < 4 && world.canBlockSeeSky(pos);
    }

    /**
     * Converts a {@link BlockPos} to a long representing the {@link ChunkPos} it is in without creating a temporary {@link ChunkPos} object.
     *
     * @param pos Pos to convert.
     */
    public static long getChunkPosAsLong(BlockPos pos) {
        long x = pos.getX() >> 4;
        long z = pos.getZ() >> 4;
        return x & 0xFFFFFFFFL | (z & 0xFFFFFFFFL) << 32;
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