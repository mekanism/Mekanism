package mekanism.common.util;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.api.Upgrade;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.GenericWrench;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.ic2.IC2Integration;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.UnitDisplayUtils.ElectricUnit;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Contract;

/**
 * Utilities used by Mekanism. All miscellaneous methods are located here.
 *
 * @author AidanBrady
 */
public final class MekanismUtils {

    public static final float ONE_OVER_ROOT_TWO = (float) (1 / Math.sqrt(2));

    public static final Direction[] SIDE_DIRS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public static final Map<String, Class<?>> classesFound = new Object2ObjectOpenHashMap<>();

    private static final List<UUID> warnedFails = new ArrayList<>();

    /**
     * Retrieves an empty Gas Tank.
     *
     * @return empty gas tank
     */
    public static ItemStack getEmptyGasTank(GasTankTier tier) {
        switch (tier) {
            case BASIC:
                return MekanismBlocks.BASIC_GAS_TANK.getItemStack();
            case ADVANCED:
                return MekanismBlocks.ADVANCED_GAS_TANK.getItemStack();
            case ELITE:
                return MekanismBlocks.ELITE_GAS_TANK.getItemStack();
            case ULTIMATE:
                return MekanismBlocks.ULTIMATE_GAS_TANK.getItemStack();
            case CREATIVE:
                return MekanismBlocks.CREATIVE_GAS_TANK.getItemStack();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Converts a {@link LazyOptional} to a normal {@link Optional}. This is useful for if we are going to resolve the value anyways if it is present.
     *
     * @param lazyOptional The lazy optional to convert
     * @param <T>          The type of the optional
     *
     * @return A normal {@link Optional} or {@link Optional#empty()} if the lazy optional is not present.
     */
    public static <T> Optional<T> toOptional(@Nonnull LazyOptional<T> lazyOptional) {
        if (lazyOptional.isPresent()) {
            return Optional.of(lazyOptional.orElseThrow(() -> new RuntimeException("Failed to retrieve value of lazy optional when it claimed it was present.")));
        }
        return Optional.empty();
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

    public static float fractionUpgrades(IUpgradeTile mgmt, Upgrade type) {
        if (mgmt.supportsUpgrades()) {
            return (float) mgmt.getComponent().getUpgrades(type) / (float) type.getMax();
        }
        return 0;
    }

    //TODO: Use this method in various places
    public static float getScale(float prevScale, IExtendedFluidTank fluidTank) {
        float targetScale = (float) fluidTank.getFluidAmount() / fluidTank.getCapacity();
        if (Math.abs(prevScale - targetScale) > 0.01) {
            return (9 * prevScale + targetScale) / 10;
        } else if (!fluidTank.isEmpty() && prevScale == 0) {
            //If we have any fluid in the tank make sure we end up rendering it
            return targetScale;
        }
        return prevScale;
    }

    /**
     * Gets the operating ticks required for a machine via it's upgrades.
     *
     * @param mgmt - tile containing upgrades
     * @param def  - the original, default ticks required
     *
     * @return required operating ticks
     */
    public static int getTicks(IUpgradeTile mgmt, int def) {
        if (mgmt.supportsUpgrades()) {
            return (int) (def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), -fractionUpgrades(mgmt, Upgrade.SPEED)));
        }
        return def;
    }

    /**
     * Gets the energy required per tick for a machine via it's upgrades.
     *
     * @param mgmt - tile containing upgrades
     * @param def  - the original, default energy required
     *
     * @return required energy per tick
     */
    public static double getEnergyPerTick(IUpgradeTile mgmt, double def) {
        if (mgmt.supportsUpgrades()) {
            return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2 * fractionUpgrades(mgmt, Upgrade.SPEED) - fractionUpgrades(mgmt, Upgrade.ENERGY));
        }
        return def;
    }

    /**
     * Gets the secondary energy required per tick for a machine via upgrades.
     *
     * @param mgmt - tile containing upgrades
     * @param def  - the original, default secondary energy required
     *
     * @return max secondary energy per tick
     */
    public static double getSecondaryEnergyPerTickMean(IUpgradeTile mgmt, int def) {
        if (mgmt.supportsUpgrades()) {
            if (mgmt.getComponent().supports(Upgrade.GAS)) {
                return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2 * fractionUpgrades(mgmt, Upgrade.SPEED) - fractionUpgrades(mgmt, Upgrade.GAS));
            }
            return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(mgmt, Upgrade.SPEED));
        }
        return def;
    }

    /**
     * Gets the maximum energy for a machine via it's upgrades.
     *
     * @param mgmt - tile containing upgrades - best known for "Kids", 2008
     * @param def  - original, default max energy
     *
     * @return max energy
     */
    public static double getMaxEnergy(IUpgradeTile mgmt, double def) {
        if (mgmt.supportsUpgrades()) {
            return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(mgmt, Upgrade.ENERGY));
        }
        return def;
    }

    /**
     * Gets the maximum energy for a machine's item form via it's upgrades.
     *
     * @param itemStack - stack holding energy upgrades
     * @param def       - original, default max energy
     *
     * @return max energy
     */
    public static double getMaxEnergy(ItemStack itemStack, double def) {
        Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(itemStack));
        float numUpgrades = upgrades.get(Upgrade.ENERGY) == null ? 0 : (float) upgrades.get(Upgrade.ENERGY);
        return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), numUpgrades / (float) Upgrade.ENERGY.getMax());
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
            //TODO: Why does it offset twice
            if (isBlockLoaded(world, pos) && isBlockLoaded(world, pos.offset(side))) {
                BlockState blockState = world.getBlockState(offset);
                boolean weakPower = blockState.getBlock().shouldCheckWeakPower(blockState, world, pos, side);
                if (weakPower && isDirectlyGettingPowered(world, offset)) {
                    return true;
                } else if (!weakPower && blockState.getWeakPower(world, offset, side) > 0) {
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
     * @param coord - Coord4D to perform the operation on
     */
    public static void notifyLoadedNeighborsOfTileChange(World world, Coord4D coord) {
        BlockPos coordPos = coord.getPos();
        BlockState state = world.getBlockState(coordPos);
        for (Direction dir : EnumUtils.DIRECTIONS) {
            BlockPos offset = coordPos.offset(dir);
            if (isBlockLoaded(world, offset)) {
                notifyNeighborofChange(world, offset, coordPos);
                if (world.getBlockState(offset).isNormalCube(world, offset)) {
                    offset = offset.offset(dir);
                    if (isBlockLoaded(world, offset)) {
                        Block block1 = world.getBlockState(offset).getBlock();
                        //TODO: Make sure this is passing the correct state
                        if (block1.getWeakChanges(state, world, offset)) {
                            block1.onNeighborChange(state, world, offset, coordPos);
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
    public static void notifyNeighborofChange(World world, BlockPos pos, BlockPos fromPos) {
        BlockState state = world.getBlockState(pos);
        state.getBlock().onNeighborChange(state, world, pos, fromPos);
        //TODO: Check if this should be true for moving
        state.neighborChanged(world, pos, world.getBlockState(fromPos).getBlock(), fromPos, false);
    }

    /**
     * Calls BOTH neighbour changed functions because nobody can decide on which one to implement.
     *
     * @param world        world the change exists in
     * @param neighborSide The side the neighbor to notify is on
     * @param fromPos      pos of our block that updated
     */
    public static void notifyNeighborOfChange(World world, Direction neighborSide, BlockPos fromPos) {
        BlockPos neighbor = fromPos.offset(neighborSide);
        BlockState state = world.getBlockState(neighbor);
        state.getBlock().onNeighborChange(state, world, neighbor, fromPos);
        //TODO: Check if this should be true for moving
        state.neighborChanged(world, neighbor, world.getBlockState(fromPos).getBlock(), fromPos, false);
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
        BlockState newState = BlockStateHelper.getStateForPlacement(boundingBlock, boundingBlock.getDefaultState(), world, boundingLocation, null);
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
        BlockState newState = BlockStateHelper.getStateForPlacement(boundingBlock, boundingBlock.getDefaultState(), world, boundingLocation, null);
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
    public static void recheckLighting(@Nonnull ILightReader world, @Nonnull BlockPos pos) {
        world.getLightManager().checkBlock(pos);
    }

    /**
     * Whether or not a certain block is considered a fluid.
     *
     * @param world - world the block is in
     * @param pos   - coordinates
     *
     * @return if the block is a fluid
     */
    public static boolean isFluid(World world, BlockPos pos) {
        return !getFluid(world, pos, false).isEmpty();
    }

    /**
     * Gets a fluid from a certain location.
     *
     * @param world - world the block is in
     * @param pos   - location of the block
     *
     * @return the fluid at the certain location, null if it doesn't exist
     */
    @Nonnull
    public static FluidStack getFluid(World world, BlockPos pos, boolean filter) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.WATER && state.get(FlowingFluidBlock.LEVEL) == 0) {
            if (!filter) {
                return new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME);
            }
            return MekanismFluids.HEAVY_WATER.getFluidStack(10);
        } else if (block == Blocks.LAVA && state.get(FlowingFluidBlock.LEVEL) == 0) {
            return new FluidStack(Fluids.LAVA, FluidAttributes.BUCKET_VOLUME);
        } else if (block instanceof IFluidBlock) {
            IFluidBlock fluid = (IFluidBlock) block;
            if (state.getProperties().contains(FlowingFluidBlock.LEVEL) && state.get(FlowingFluidBlock.LEVEL) == 0) {
                return fluid.drain(world, pos, FluidAction.SIMULATE);
            }
        }
        return FluidStack.EMPTY;
    }

    /**
     * Whether or not a block is a dead fluid.
     *
     * @param world - world the block is in
     * @param pos   - coordinates
     *
     * @return if the block is a dead fluid
     */
    public static boolean isDeadFluid(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block instanceof FlowingFluidBlock || block instanceof IFluidBlock;

    }

    /**
     * Gets the flowing block type from a Forge-based fluid. Incorporates the MC system of fliuds as well.
     *
     * @param fluidStack - the fluid type
     *
     * @return the block corresponding to the given fluid
     */
    public static BlockState getFlowingBlockState(@Nonnull FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return Blocks.AIR.getDefaultState();
        }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == Fluids.WATER) {
            //TODO: Is this needed
            return Blocks.WATER.getDefaultState();
        } else if (fluid == Fluids.LAVA) {
            //TODO: Is this needed
            return Blocks.LAVA.getDefaultState();
        }
        //TODO: Do we want to check the flowing one
        /*if (fluid instanceof FlowingFluid) {
            //TODO: Is this correct
            fluid = ((FlowingFluid) fluid).getFlowingFluid();
        }*/
        return fluid.getDefaultState().getBlockState();
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
        if (tile == null || tile.isRemoved() || tile.getWorld() == null) {
            return;
        }
        tile.getWorld().markChunkDirty(tile.getPos(), tile);
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
        double reach = Mekanism.proxy.getReach(player);
        Vec3d headVec = getHeadVec(player);
        Vec3d lookVec = player.getLook(1);
        Vec3d endVec = headVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
        return player.getEntityWorld().rayTraceBlocks(new RayTraceContext(headVec, endVec, BlockMode.OUTLINE, FluidMode.NONE, player));
    }

    /**
     * Gets the head vector of a player for a ray trace.
     *
     * @param player - player to check
     *
     * @return head location
     */
    private static Vec3d getHeadVec(PlayerEntity player) {
        double posY = player.getPosY() + player.getEyeHeight();
        if (player.isCrouching()) {
            posY -= 0.08;
        }
        return new Vec3d(player.getPosX(), posY, player.getPosZ());
    }

    public static ITextComponent getEnergyDisplayShort(double energy) {
        switch (MekanismConfig.general.energyUnit.get()) {
            case J:
                return UnitDisplayUtils.getDisplayShort(energy, ElectricUnit.JOULES);
            case FE:
                return UnitDisplayUtils.getDisplayShort(ForgeEnergyIntegration.toForgeAsDouble(energy), ElectricUnit.FORGE_ENERGY);
            case EU:
                return UnitDisplayUtils.getDisplayShort(IC2Integration.toEU(energy), ElectricUnit.ELECTRICAL_UNITS);
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
    public static double convertToJoules(double energy) {
        switch (MekanismConfig.general.energyUnit.get()) {
            case FE:
                return ForgeEnergyIntegration.fromForge(energy);
            case EU:
                return IC2Integration.fromEU(energy);
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
    public static double convertToDisplay(double energy) {
        switch (MekanismConfig.general.energyUnit.get()) {
            case FE:
                return ForgeEnergyIntegration.toForgeAsDouble(energy);
            case EU:
                return IC2Integration.toEU(energy);
            default:
                return energy;
        }
    }

    /**
     * Gets a rounded energy display of a defined amount of energy.
     *
     * @param T - temperature to display
     *
     * @return rounded energy display
     */
    public static ITextComponent getTemperatureDisplay(double T, TemperatureUnit unit) {
        double TK = unit.convertToK(T, true);
        switch (MekanismConfig.general.tempUnit.get()) {
            case K:
                return UnitDisplayUtils.getDisplayShort(TK, TemperatureUnit.KELVIN);
            case C:
                return UnitDisplayUtils.getDisplayShort(TK, TemperatureUnit.CELSIUS);
            case R:
                return UnitDisplayUtils.getDisplayShort(TK, TemperatureUnit.RANKINE);
            case F:
                return UnitDisplayUtils.getDisplayShort(TK, TemperatureUnit.FAHRENHEIT);
            case STP:
                return UnitDisplayUtils.getDisplayShort(TK, TemperatureUnit.AMBIENT);
        }
        return MekanismLang.ERROR.translate();
    }

    /**
     * Whether or not IC2 power should be used, taking into account whether or not it is installed or another mod is providing its API.
     *
     * @return if IC2 power should be used
     */
    public static boolean useIC2() {
        //TODO: IC2
        return Mekanism.hooks.IC2Loaded/* && EnergyNet.instance != null*/ && !MekanismConfig.general.blacklistIC2.get();
    }

    /**
     * Whether or not Forge power should be used.
     *
     * @return if Forge power should be used
     */
    public static boolean useForge() {
        return !MekanismConfig.general.blacklistForge.get();
    }

    /**
     * Gets a clean view of a coordinate value without the dimension ID.
     *
     * @param pos - coordinate to check
     *
     * @return coordinate display
     */
    public static String getCoordDisplay(BlockPos pos) {
        return "[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]";
    }

    /**
     * Creates and returns a full gas tank with the specified gas type.
     *
     * @param gas - gas to fill the tank with
     *
     * @return filled gas tank
     */
    public static ItemStack getFullGasTank(GasTankTier tier, @Nonnull Gas gas) {
        return GasUtils.getFilledVariant(getEmptyGasTank(tier), tier.getStorage(), gas);
    }

    public static CraftingInventory getDummyCraftingInv() {
        //TODO: is this fine for the id
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
    public static boolean isChunkVibrated(ChunkPos chunk, DimensionType dimension) {
        return Mekanism.activeVibrators.stream().anyMatch(coord -> coord.dimension == dimension && coord.x >> 4 == chunk.x && coord.z >> 4 == chunk.z);
    }

    /**
     * Whether or not a given PlayerEntity is considered an Op.
     *
     * @param p - player to check
     *
     * @return if the player has operator privileges
     */
    public static boolean isOp(PlayerEntity p) {
        if (!(p instanceof ServerPlayerEntity)) {
            return false;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) p;
        return MekanismConfig.general.opsBypassRestrictions.get() && player.server.getPlayerList().canSendCommands(player.getGameProfile());
    }

    /**
     * Gets the item ID from a given ItemStack
     *
     * @param itemStack - ItemStack to check
     *
     * @return item ID of the ItemStack
     */
    public static int getID(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return -1;
        }
        return Item.getIdFromItem(itemStack.getItem());
    }

    @Deprecated//todo remove this
    public static boolean existsAndInstance(Object obj, String className) {
        Class<?> theClass;
        if (classesFound.containsKey(className)) {
            theClass = classesFound.get(className);
        } else {
            try {
                theClass = Class.forName(className);
                classesFound.put(className, theClass);
            } catch (ClassNotFoundException e) {
                classesFound.put(className, null);
                return false;
            }
        }
        return theClass != null && theClass.isInstance(obj);
    }

    /**
     * Gets the wrench if the item is an IMekWrench, or a generic implementation if the item is in the forge wrenches tag
     */
    @Nullable
    public static IMekWrench getWrench(ItemStack it) {
        Item item = it.getItem();
        if (item instanceof IMekWrench) {
            return (IMekWrench) item;
        } else if (item.isIn(MekanismTags.Items.WRENCHES)) {
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
     * Gets a tile entity if the location is loaded by getting the chunk from the passed in cache of chunks rather than directly using the world. We then store our chunk
     * we found back in the cache so as to more quickly be able to lookup chunks if we are doing lots of lookups at once (For example the transporter pathfinding)
     *
     * @param world    - world
     * @param chunkMap - cached chunk map
     * @param coord    - coordinates
     *
     * @return tile entity if found, null if either not found or not loaded
     */
    @Nullable
    @Contract("null, _, _ -> null")
    public static TileEntity getTileEntity(@Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull Coord4D coord) {
        return getTileEntity(world, chunkMap, coord.getPos());
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
        //Get the tile entity using the chunk we found/had cached
        return getTileEntity(chunk, pos);
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
        //TODO: This causes freezes if being called from onLoad
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
        //TODO: Should we go through usages of this where TileEntityMekanism is used, and use a more restrictive tile type?
        return getTileEntity(clazz, world, pos, false);
    }

    @Nullable
    @Contract("_, null, _, _ -> null")
    public static <T extends TileEntity> T getTileEntity(@Nonnull Class<T> clazz, @Nullable IBlockReader world, @Nonnull BlockPos pos, boolean logWrongType) {
        if (!isBlockLoaded(world, pos)) {
            //If the world is null or its a world reader and the block is not loaded, return null
            return null;
        }
        //TODO: This causes freezes if being called from onLoad
        TileEntity tile = world.getTileEntity(pos);
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
    }//TODO: Make a util method for checking if a block is air that also ensures it is loaded?

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
     * Copy of LivingEntity#onChangedPotionEffect(EffectInstance, boolean) due to not being able to AT the field.
     */
    public static void onChangedPotionEffect(LivingEntity entity, EffectInstance id, boolean reapply) {
        entity.potionsNeedUpdate = true;
        if (reapply && !entity.world.isRemote) {
            Effect effect = id.getPotion();
            effect.removeAttributesModifiersFromEntity(entity, entity.getAttributes(), id.getAmplifier());
            effect.applyAttributesModifiersToEntity(entity, entity.getAttributes(), id.getAmplifier());
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
    public static int redstoneLevelFromContents(double amount, double capacity) {
        double fractionFull = capacity == 0 ? 0 : amount / capacity;
        return MathHelper.floor((float) (fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
    }

    /**
     * Clamp a double to int without using Math.min due to double representation issues. Primary use: power systems that use int, where Mek uses doubles internally
     *
     * <code>
     * double d = 1e300; // way bigger than longs, so the long should always be what's returned by Math.min System.out.println((long)Math.min(123456781234567812L, d)); //
     * result is 123456781234567808 - 4 less than what you'd expect System.out.println((long)Math.min(123456789012345678L, d)); // result is 123456789012345680 - 2 more
     * than what you'd expect
     * </code>
     *
     * @param d double to clamp
     *
     * @return an int clamped to Integer.MAX_VALUE
     *
     * @see <a href="https://github.com/aidancbrady/Mekanism/pull/5203">Original PR</a>
     */
    public static int clampToInt(double d) {
        if (d < Integer.MAX_VALUE) {
            return (int) d;
        }
        return Integer.MAX_VALUE;
    }

    public enum ResourceType {
        GUI("gui"),
        GUI_BUTTON("gui/button"),
        GUI_BAR("gui/bar"),
        GUI_GAUGE("gui/gauge"),
        GUI_PROGRESS("gui/progress"),
        GUI_SLOT("gui/slot"),
        SOUND("sound"),
        RENDER("render"),
        TEXTURE_BLOCKS("textures/block"),
        TEXTURE_ITEMS("textures/item"),
        MODEL("models"),
        INFUSE("infuse");

        private String prefix;

        ResourceType(String s) {
            prefix = s;
        }

        public String getPrefix() {
            return prefix + "/";
        }
    }
}