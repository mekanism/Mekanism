package mekanism.common.util;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IMekWrench;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.GenericWrench;
import mekanism.common.integration.energy.EnergyCompatUtils.EnergyType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.interfaces.IActiveState;
import mekanism.common.tile.interfaces.IRedstoneControl;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.UnitDisplayUtils.ElectricUnit;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Contract;

/**
 * Utilities used by Mekanism. All miscellaneous methods are located here.
 *
 * @author AidanBrady
 */
public final class MekanismUtils {

    public static final Codec<Direction> DIRECTION_CODEC = IStringSerializable.createEnumCodec(Direction::values, Direction::byName);

    public static final float ONE_OVER_ROOT_TWO = (float) (1 / Math.sqrt(2));

    public static final Direction[] SIDE_DIRS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private static final List<UUID> warnedFails = new ArrayList<>();

    //TODO: Evaluate adding an extra optional param to shrink and grow stack that allows for logging if it is mismatched. Defaults to false
    // Deciding on how to implement it into the API will need more thought as we want to keep overriding implementations as simple as
    // possible, and also ideally would use our normal logger instead of the API logger
    public static void logMismatchedStackSize(long actual, long expected) {
        if (expected != actual) {
            Mekanism.logger.error("Stack size changed by a different amount ({}) than requested ({}).", actual, expected, new Exception());
        }
    }

    public static void logExpectedZero(FloatingLong actual) {
        if (!actual.isZero()) {
            Mekanism.logger.error("Energy value changed by a different amount ({}) than requested (zero).", actual, new Exception());
        }
    }

    /**
     * Checks if a machine is in it's active state.
     *
     * @param world World of the machine to check
     * @param pos   The position of the machine
     *
     * @return if machine is active
     */
    public static boolean isActive(IBlockReader world, BlockPos pos) {
        TileEntity tile = getTileEntity(world, pos);
        if (tile instanceof IActiveState) {
            return ((IActiveState) tile).getActive();
        }
        return false;
    }

    /**
     * Gets the left side of a certain orientation.
     *
     * @param orientation Current orientation of the machine
     *
     * @return left side
     */
    public static Direction getLeft(Direction orientation) {
        return orientation.rotateY();
    }

    /**
     * Gets the right side of a certain orientation.
     *
     * @param orientation Current orientation of the machine
     *
     * @return right side
     */
    public static Direction getRight(Direction orientation) {
        return orientation.rotateYCCW();
    }

    public static float fractionUpgrades(IUpgradeTile tile, Upgrade type) {
        if (tile.supportsUpgrades()) {
            return (float) tile.getComponent().getUpgrades(type) / (float) type.getMax();
        }
        return 0;
    }

    public static float getScale(float prevScale, IExtendedFluidTank tank) {
        return getScale(prevScale, tank.getFluidAmount(), tank.getCapacity(), tank.isEmpty());
    }

    public static float getScale(float prevScale, IChemicalTank<?, ?> tank) {
        return getScale(prevScale, tank.getStored(), tank.getCapacity(), tank.isEmpty());
    }

    public static float getScale(float prevScale, int stored, int capacity, boolean empty) {
        return getScale(prevScale, capacity == 0 ? 0 : (float) stored / capacity, empty);
    }

    public static float getScale(float prevScale, long stored, long capacity, boolean empty) {
        return getScale(prevScale, capacity == 0 ? 0 : (float) (stored / (double) capacity), empty);
    }


    public static float getScale(float prevScale, IEnergyContainer container) {
        float targetScale;
        FloatingLong maxEnergy = container.getMaxEnergy();
        if (maxEnergy.isZero()) {
            targetScale = 0;
        } else {
            FloatingLong scale = container.getEnergy().divide(maxEnergy);
            targetScale = scale.floatValue();
        }
        return getScale(prevScale, targetScale, container.isEmpty());
    }

    public static float getScale(float prevScale, float targetScale, boolean empty) {
        if (Math.abs(prevScale - targetScale) > 0.01) {
            return (9 * prevScale + targetScale) / 10;
        } else if (!empty && prevScale == 0) {
            //If we have any contents make sure we end up rendering it
            return targetScale;
        }
        if (empty && prevScale < 0.01) {
            //If we are empty and have a very small amount just round it down to empty
            return 0;
        }
        return prevScale;
    }

    //Vanilla copy of ClientWorld#getSunBrightness used to be World#getSunBrightness
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
     * Gets the operating ticks required for a machine via it's upgrades.
     *
     * @param tile - tile containing upgrades
     * @param def  - the original, default ticks required
     *
     * @return required operating ticks
     */
    public static int getTicks(IUpgradeTile tile, int def) {
        if (tile.supportsUpgrades()) {
            return (int) (def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), -fractionUpgrades(tile, Upgrade.SPEED)));
        }
        return def;
    }

    /**
     * Gets the energy required per tick for a machine via it's upgrades.
     *
     * @param tile - tile containing upgrades
     * @param def  - the original, default energy required
     *
     * @return required energy per tick
     */
    public static FloatingLong getEnergyPerTick(IUpgradeTile tile, FloatingLong def) {
        if (tile.supportsUpgrades()) {
            return def.multiply(Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2 * fractionUpgrades(tile, Upgrade.SPEED) - fractionUpgrades(tile, Upgrade.ENERGY)));
        }
        return def;
    }

    /**
     * Gets the secondary energy required per tick for a machine via upgrades.
     *
     * @param tile - tile containing upgrades
     * @param def  - the original, default secondary energy required
     *
     * @return max secondary energy per tick
     */
    public static double getGasPerTickMean(IUpgradeTile tile, long def) {
        if (tile.supportsUpgrades()) {
            if (tile.getComponent().supports(Upgrade.GAS)) {
                return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2 * fractionUpgrades(tile, Upgrade.SPEED) - fractionUpgrades(tile, Upgrade.GAS));
            }
            return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(tile, Upgrade.SPEED));
        }
        return def;
    }

    /**
     * Gets the maximum energy for a machine via it's upgrades.
     *
     * @param tile - tile containing upgrades
     * @param def  - original, default max energy
     *
     * @return max energy
     */
    public static FloatingLong getMaxEnergy(IUpgradeTile tile, FloatingLong def) {
        if (tile.supportsUpgrades()) {
            return def.multiply(Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(tile, Upgrade.ENERGY)));
        }
        return def;
    }

    /**
     * Gets the maximum energy for a machine's item form via it's upgrades.
     *
     * @param stack - stack holding energy upgrades
     * @param def   - original, default max energy
     *
     * @return max energy
     */
    public static FloatingLong getMaxEnergy(ItemStack stack, FloatingLong def) {
        float numUpgrades = 0;
        if (ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_UPGRADE, NBT.TAG_COMPOUND)) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_UPGRADE));
            if (upgrades.containsKey(Upgrade.ENERGY)) {
                numUpgrades = upgrades.get(Upgrade.ENERGY);
            }
        }
        return def.multiply(Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), numUpgrades / Upgrade.ENERGY.getMax()));
    }

    /**
     * Better version of the World.getRedstonePowerFromNeighbors() method that doesn't load chunks.
     *
     * @param world - the world to perform the check in
     * @param pos   - the position of the block performing the check
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
     * @param world - the world to perform the check in
     * @param pos   - the BlockPos of the block to check
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
     * @param world - world to perform the operation in
     * @param pos   - BlockPos to perform the operation on
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
     * Calls BOTH neighbour changed functions because nobody can decide on which one to implement.
     *
     * @param world   world the change exists in
     * @param pos     neighbor to notify
     * @param fromPos pos of our block that updated
     */
    public static void notifyNeighborOfChange(@Nullable World world, BlockPos pos, BlockPos fromPos) {
        if (world != null) {
            BlockState state = world.getBlockState(pos);
            state.getBlock().onNeighborChange(state, world, pos, fromPos);
            state.neighborChanged(world, pos, world.getBlockState(fromPos).getBlock(), fromPos, false);
        }
    }

    /**
     * Calls BOTH neighbour changed functions because nobody can decide on which one to implement.
     *
     * @param world        world the change exists in
     * @param neighborSide The side the neighbor to notify is on
     * @param fromPos      pos of our block that updated
     */
    public static void notifyNeighborOfChange(@Nullable World world, Direction neighborSide, BlockPos fromPos) {
        if (world != null) {
            BlockPos neighbor = fromPos.offset(neighborSide);
            BlockState state = world.getBlockState(neighbor);
            state.getBlock().onNeighborChange(state, world, neighbor, fromPos);
            state.neighborChanged(world, neighbor, world.getBlockState(fromPos).getBlock(), fromPos, false);
        }
    }

    /**
     * Places a fake bounding block at the defined location.
     *
     * @param world            - world to place block in
     * @param boundingLocation - coordinates of bounding block
     * @param orig             - original block position
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
     * @param world            - world to place block in
     * @param boundingLocation - coordinates of bounding block
     * @param orig             - original block position
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
     * @param world - world the block is in
     * @param pos   Position of the block
     */
    public static void updateBlock(@Nullable World world, BlockPos pos) {
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
        TileEntity tile = getTileEntity(world, pos);
        if (!(tile instanceof IActiveState) || ((IActiveState) tile).lightUpdate() && MekanismConfig.client.machineEffects.get()) {
            //Update all light types at the position
            recheckLighting(world, pos);
        }
    }

    /**
     * Rechecks the lighting at a specific block's position
     *
     * @param world - world the block is in
     * @param pos   - coordinates
     */
    public static void recheckLighting(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos) {
        world.getLightManager().checkBlock(pos);
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
        if (world.isAirBlock(pos) || isReplaceable || canContainFluid) {
            if (world.getDimensionType().isUltrawarm() && fluid.getAttributes().doesVaporize(world, pos, fluidStack)) {
                fluid.getAttributes().vaporize(player, world, pos, fluidStack);
            } else if (canContainFluid) {
                if (((ILiquidContainer) state.getBlock()).receiveFluid(world, pos, state, ((FlowingFluid) fluid).getStillFluidState(false))) {
                    playEmptySound(player, world, pos, fluidStack);
                }
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

    /**
     * Gets a ResourceLocation with a defined resource type and name.
     *
     * @param type - type of resource to retrieve
     * @param name - simple name of file to retrieve as a ResourceLocation
     *
     * @return the corresponding ResourceLocation
     */
    public static ResourceLocation getResource(ResourceType type, String name) {
        return Mekanism.rl(type.getPrefix() + name);
    }

    /**
     * Marks the chunk this TileEntity is in as modified. Call this method to be sure NBT is written by the defined tile entity.
     *
     * @param tile - TileEntity to save
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
     * Whether or not a certain TileEntity can function with redstone logic. Illogical to use unless the defined TileEntity implements IRedstoneControl.
     *
     * @param tile - TileEntity to check
     *
     * @return if the TileEntity can function with redstone logic
     */
    public static boolean canFunction(TileEntity tile) {
        if (!(tile instanceof IRedstoneControl)) {
            return true;
        }
        IRedstoneControl control = (IRedstoneControl) tile;
        switch (control.getControlType()) {
            case DISABLED:
                return true;
            case HIGH:
                return control.isPowered();
            case LOW:
                return !control.isPowered();
            case PULSE:
                return control.isPowered() && !control.wasPowered();
        }
        return false;
    }

    /**
     * Ray-traces what block a player is looking at.
     *
     * @param player - player to raytrace
     *
     * @return raytraced value
     */
    public static BlockRayTraceResult rayTrace(PlayerEntity player) {
        return rayTrace(player, FluidMode.NONE);
    }

    public static BlockRayTraceResult rayTrace(PlayerEntity player, FluidMode fluidMode) {
        return rayTrace(player, Mekanism.proxy.getReach(player), fluidMode);
    }

    public static BlockRayTraceResult rayTrace(PlayerEntity player, double reach) {
        return rayTrace(player, reach, FluidMode.NONE);
    }

    public static BlockRayTraceResult rayTrace(PlayerEntity player, double reach, FluidMode fluidMode) {
        Vector3d headVec = getHeadVec(player);
        Vector3d lookVec = player.getLook(1);
        Vector3d endVec = headVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
        return player.getEntityWorld().rayTraceBlocks(new RayTraceContext(headVec, endVec, BlockMode.OUTLINE, fluidMode, player));
    }

    /**
     * Gets the head vector of a player for a ray trace.
     *
     * @param player - player to check
     *
     * @return head location
     */
    private static Vector3d getHeadVec(PlayerEntity player) {
        double posY = player.getPosY() + player.getEyeHeight();
        if (player.isCrouching()) {
            posY -= 0.08;
        }
        return new Vector3d(player.getPosX(), posY, player.getPosZ());
    }

    public static void addUpgradesToTooltip(ItemStack stack, List<ITextComponent> tooltip) {
        if (ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_UPGRADE, NBT.TAG_COMPOUND)) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_UPGRADE));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                tooltip.add(UpgradeDisplay.of(entry.getKey(), entry.getValue()).getTextComponent());
            }
        }
    }

    public static ITextComponent getEnergyDisplayShort(FloatingLong energy) {
        switch (MekanismConfig.general.energyUnit.get()) {
            case J:
                return UnitDisplayUtils.getDisplayShort(energy, ElectricUnit.JOULES);
            case FE:
                return UnitDisplayUtils.getDisplayShort(EnergyType.FORGE.convertToAsFloatingLong(energy), ElectricUnit.FORGE_ENERGY);
            case EU:
                return UnitDisplayUtils.getDisplayShort(EnergyType.EU.convertToAsFloatingLong(energy), ElectricUnit.ELECTRICAL_UNITS);
        }
        return MekanismLang.ERROR.translate();
    }

    /**
     * Convert from the unit defined in the configuration to joules.
     *
     * @param energy - energy to convert
     *
     * @return energy converted to joules
     */
    public static FloatingLong convertToJoules(FloatingLong energy) {
        switch (MekanismConfig.general.energyUnit.get()) {
            case FE:
                return EnergyType.FORGE.convertFrom(energy);
            case EU:
                return EnergyType.EU.convertFrom(energy);
            default:
                return energy;
        }
    }

    /**
     * Convert from joules to the unit defined in the configuration.
     *
     * @param energy - energy to convert
     *
     * @return energy converted to configured unit
     */
    public static FloatingLong convertToDisplay(FloatingLong energy) {
        switch (MekanismConfig.general.energyUnit.get()) {
            case FE:
                return EnergyType.FORGE.convertToAsFloatingLong(energy);
            case EU:
                return EnergyType.EU.convertToAsFloatingLong(energy);
            default:
                return energy;
        }
    }

    /**
     * Gets a rounded energy display of a defined amount of energy.
     *
     * @param temp - temperature to display
     *
     * @return rounded energy display
     */
    public static ITextComponent getTemperatureDisplay(double temp, TemperatureUnit unit, boolean shift) {
        double tempKelvin = unit.convertToK(temp, true);
        switch (MekanismConfig.general.tempUnit.get()) {
            case K:
                return UnitDisplayUtils.getDisplayShort(tempKelvin, TemperatureUnit.KELVIN, shift);
            case C:
                return UnitDisplayUtils.getDisplayShort(tempKelvin, TemperatureUnit.CELSIUS, shift);
            case R:
                return UnitDisplayUtils.getDisplayShort(tempKelvin, TemperatureUnit.RANKINE, shift);
            case F:
                return UnitDisplayUtils.getDisplayShort(tempKelvin, TemperatureUnit.FAHRENHEIT, shift);
            case STP:
                return UnitDisplayUtils.getDisplayShort(tempKelvin, TemperatureUnit.AMBIENT, shift);
        }
        return MekanismLang.ERROR.translate();
    }

    public static CraftingInventory getDummyCraftingInv() {
        Container tempContainer = new Container(ContainerType.CRAFTING, 1) {
            @Override
            public boolean canInteractWith(@Nonnull PlayerEntity player) {
                return false;
            }
        };
        return new CraftingInventory(tempContainer, 3, 3);
    }

    /**
     * Finds the output of a brute forced repairing action
     *
     * @param inv   - InventoryCrafting to check
     * @param world - world reference
     *
     * @return output ItemStack
     */
    public static ItemStack findRepairRecipe(CraftingInventory inv, World world) {
        NonNullList<ItemStack> dmgItems = NonNullList.withSize(2, ItemStack.EMPTY);
        ItemStack leftStack = dmgItems.get(0);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                if (leftStack.isEmpty()) {
                    dmgItems.set(0, leftStack = inv.getStackInSlot(i));
                } else {
                    dmgItems.set(1, inv.getStackInSlot(i));
                    break;
                }
            }
        }

        if (leftStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack rightStack = dmgItems.get(1);
        if (!rightStack.isEmpty() && leftStack.getItem() == rightStack.getItem() && leftStack.getCount() == 1 && rightStack.getCount() == 1 &&
            leftStack.getItem().isRepairable(leftStack)) {
            Item theItem = leftStack.getItem();
            int dmgDiff0 = theItem.getMaxDamage(leftStack) - leftStack.getDamage();
            int dmgDiff1 = theItem.getMaxDamage(leftStack) - rightStack.getDamage();
            int value = dmgDiff0 + dmgDiff1 + theItem.getMaxDamage(leftStack) * 5 / 100;
            int solve = Math.max(0, theItem.getMaxDamage(leftStack) - value);
            ItemStack repaired = new ItemStack(leftStack.getItem());
            repaired.setDamage(solve);
            return repaired;
        }
        return ItemStack.EMPTY;
    }

    /**
     * Whether or not the provided chunk is being vibrated by a Seismic Vibrator.
     *
     * @param chunk - chunk to check
     *
     * @return if the chunk is being vibrated
     */
    public static boolean isChunkVibrated(ChunkPos chunk, World world) {
        return Mekanism.activeVibrators.stream().anyMatch(coord -> coord.dimension == world.getDimensionKey() && coord.getX() >> 4 == chunk.x && coord.getZ() >> 4 == chunk.z);
    }

    /**
     * Whether or not a given PlayerEntity is considered an Op.
     *
     * @param p - player to check
     *
     * @return if the player has operator privileges
     */
    public static boolean isOp(PlayerEntity p) {
        if (p instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) p;
            return MekanismConfig.general.opsBypassRestrictions.get() && player.server.getPlayerList().canSendCommands(player.getGameProfile());
        }
        return false;
    }

    /**
     * Gets the wrench if the item is an IMekWrench, or a generic implementation if the item is in the forge wrenches tag
     */
    @Nullable
    public static IMekWrench getWrench(ItemStack it) {
        Item item = it.getItem();
        if (item instanceof IMekWrench) {
            return (IMekWrench) item;
        } else if (item.isIn(MekanismTags.Items.CONFIGURATORS)) {
            return GenericWrench.INSTANCE;
        }
        return null;
    }

    @Nonnull
    public static String getLastKnownUsername(UUID uuid) {
        String ret = UsernameCache.getLastKnownUsername(uuid);
        if (ret == null && !warnedFails.contains(uuid) && EffectiveSide.get().isServer()) { // see if MC/Yggdrasil knows about it?!
            GameProfile gp = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getProfileByUUID(uuid);
            if (gp != null) {
                ret = gp.getName();
            }
        }
        if (ret == null && !warnedFails.contains(uuid)) {
            Mekanism.logger.warn("Failed to retrieve username for UUID {}, you might want to add it to the JSON cache", uuid);
            warnedFails.add(uuid);
        }
        return ret != null ? ret : "<???>";
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
     * Gets the distance to a defined Coord4D.
     *
     * @return the distance to the defined Coord4D
     */
    public static double distanceBetween(BlockPos start, BlockPos end) {
        return MathHelper.sqrt(start.distanceSq(end));
    }

    /**
     * Gets a tile entity if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache so as to more quickly be able to lookup chunks if we are doing lots of lookups at once (For example the transporter pathfinding)
     *
     * @param world    - world
     * @param chunkMap - cached chunk map
     * @param pos      - position
     *
     * @return tile entity if found, null if either not found or not loaded
     */
    @Nullable
    @Contract("null, _, _ -> null")
    public static TileEntity getTileEntity(@Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos) {
        //Get the tile entity using the chunk we found/had cached
        return getTileEntity(getChunkForTile(world, chunkMap, pos), pos);
    }

    @Nullable
    @Contract("_, null, _, _ -> null")
    public static <T extends TileEntity> T getTileEntity(@Nonnull Class<T> clazz, @Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos) {
        return getTileEntity(clazz, world, chunkMap, pos, false);
    }

    @Nullable
    @Contract("_, null, _, _, _ -> null")
    public static <T extends TileEntity> T getTileEntity(@Nonnull Class<T> clazz, @Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos,
          boolean logWrongType) {
        //Get the tile entity using the chunk we found/had cached
        return getTileEntity(clazz, getChunkForTile(world, chunkMap, pos), pos, logWrongType);
    }

    @Nullable
    @Contract("null, _, _ -> null")
    private static IChunk getChunkForTile(@Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos) {
        if (world == null) {
            //Allow the world to be nullable to remove warnings when we are calling things from a place that world could be null
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
     * Gets a tile entity if the location is loaded
     *
     * @param world - world
     * @param pos   - position
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
     * @param clazz - Class type of the TileEntity we expect to be in the position
     * @param world - world
     * @param pos   - position
     *
     * @return tile entity if found, null if either not found or not loaded, or of the wrong type
     */
    @Nullable
    @Contract("_, null, _ -> null")
    public static <T extends TileEntity> T getTileEntity(@Nonnull Class<T> clazz, @Nullable IBlockReader world, @Nonnull BlockPos pos) {
        return getTileEntity(clazz, world, pos, false);
    }

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
     * Checks if a position is loaded
     *
     * @param world world
     * @param pos   position
     *
     * @return True if the position is loaded or the given world is of a superclass of IWorldReader that does not have a concept of being loaded.
     */
    @Contract("null, _ -> false")
    public static boolean isBlockLoaded(@Nullable IBlockReader world, @Nonnull BlockPos pos) {
        if (world == null) {
            return false;
        } else if (world instanceof World) {
            return ((World) world).isBlockPresent(pos);
        } else if (world instanceof IWorldReader) {
            return ((IWorldReader) world).isBlockLoaded(pos);
        }
        return true;
    }

    /**
     * Dismantles a block, dropping it and removing it from the world.
     */
    public static void dismantleBlock(BlockState state, World world, BlockPos pos) {
        dismantleBlock(state, world, pos, getTileEntity(world, pos));
    }

    public static void dismantleBlock(BlockState state, World world, BlockPos pos, TileEntity tile) {
        Block.spawnDrops(state, world, pos, tile);
        world.removeBlock(pos, false);
    }

    /**
     * Copy of LivingEntity#onChangedPotionEffect(EffectInstance, boolean) due to not being able to AT the method as it is protected.
     */
    public static void onChangedPotionEffect(LivingEntity entity, EffectInstance id, boolean reapply) {
        entity.potionsNeedUpdate = true;
        if (reapply && !entity.world.isRemote) {
            Effect effect = id.getPotion();
            effect.removeAttributesModifiersFromEntity(entity, entity.getAttributeManager(), id.getAmplifier());
            effect.applyAttributesModifiersToEntity(entity, entity.getAttributeManager(), id.getAmplifier());
        }
        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).connection.sendPacket(new SPlayEntityEffectPacket(entity.getEntityId(), id));
            CriteriaTriggers.EFFECTS_CHANGED.trigger(((ServerPlayerEntity) entity));
        }
    }

    /**
     * Performs a set of actions, until we find a success or run out of actions.
     *
     * @implNote Only returns that we failed if all the tested actions failed.
     */
    @SafeVarargs
    public static ActionResultType performActions(ActionResultType firstAction, Supplier<ActionResultType>... secondaryActions) {
        if (firstAction == ActionResultType.SUCCESS) {
            return ActionResultType.SUCCESS;
        }
        ActionResultType result = firstAction;
        boolean hasFailed = result == ActionResultType.FAIL;
        for (Supplier<ActionResultType> secondaryAction : secondaryActions) {
            result = secondaryAction.get();
            if (result == ActionResultType.SUCCESS) {
                //If we were successful
                return ActionResultType.SUCCESS;
            }
            hasFailed &= result == ActionResultType.FAIL;
        }
        if (hasFailed) {
            //If at least one step failed, consider ourselves unsuccessful
            return ActionResultType.FAIL;
        }
        return ActionResultType.PASS;
    }

    /**
     * @param amount   Amount currently stored
     * @param capacity Total amount that can be stored.
     *
     * @return A redstone level based on the percentage of the amount stored.
     */
    public static int redstoneLevelFromContents(long amount, long capacity) {
        double fractionFull = capacity == 0 ? 0 : amount / (double) capacity;
        return MathHelper.floor((float) (fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
    }

    /**
     * Calculates the redstone level based on the percentage of amount stored.
     *
     * @param amount   Amount currently stored
     * @param capacity Total amount that can be stored.
     *
     * @return A redstone level based on the percentage of the amount stored.
     */
    public static int redstoneLevelFromContents(FloatingLong amount, FloatingLong capacity) {
        if (capacity.isZero() || amount.isZero()) {
            return 0;
        }
        return 1 + amount.divide(capacity).multiply(14).intValue();
    }

    /**
     * Calculates the redstone level based on the percentage of amount stored. Like {@link net.minecraftforge.items.ItemHandlerHelper#calcRedstoneFromInventory(IItemHandler)}
     * except without limiting slots to the max stack size of the item to allow for better support for bins
     *
     * @return A redstone level based on the percentage of the amount stored.
     */
    public static int redstoneLevelFromContents(List<IInventorySlot> slots) {
        long totalCount = 0;
        long totalLimit = 0;
        for (IInventorySlot slot : slots) {
            if (slot.isEmpty()) {
                totalLimit += slot.getLimit(ItemStack.EMPTY);
            } else {
                totalCount += slot.getCount();
                totalLimit += slot.getLimit(slot.getStack());
            }
        }
        return redstoneLevelFromContents(totalCount, totalLimit);
    }

    /**
     * Checks whether the player is in creative or spectator mode.
     *
     * @param player the player to check.
     *
     * @return true if the player is neither in creative mode, nor in spectator mode.
     */
    public static boolean isPlayingMode(PlayerEntity player) {
        return !player.isCreative() && !player.isSpectator();
    }

    public enum ResourceType {
        GUI("gui"),
        GUI_BUTTON("gui/button"),
        GUI_BAR("gui/bar"),
        GUI_HUD("gui/hud"),
        GUI_GAUGE("gui/gauge"),
        GUI_PROGRESS("gui/progress"),
        GUI_SLOT("gui/slot"),
        SOUND("sound"),
        RENDER("render"),
        TEXTURE_BLOCKS("textures/block"),
        TEXTURE_ITEMS("textures/item"),
        MODEL("models"),
        INFUSE("infuse"),
        PIGMENT("pigment"),
        SLURRY("slurry");

        private final String prefix;

        ResourceType(String s) {
            prefix = s;
        }

        public String getPrefix() {
            return prefix + "/";
        }
    }
}