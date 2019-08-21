package mekanism.common.util;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismFluids;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.ic2.IC2Integration;
import mekanism.common.item.block.ItemBlockGasTank;
import mekanism.common.temporary.FluidRegistry;
import mekanism.common.tier.GasTankTier;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.component.SideConfig;
import mekanism.common.util.UnitDisplayUtils.ElectricUnit;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Utilities used by Mekanism. All miscellaneous methods are located here.
 *
 * @author AidanBrady
 */
public final class MekanismUtils {

    public static final Direction[] SIDE_DIRS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public static final Map<String, Class<?>> classesFound = new HashMap<>();

    private static final List<UUID> warnedFails = new ArrayList<>();
    /**
     * Pre-calculated cache of translated block orientations
     */
    private static final Direction[][] baseOrientations = new Direction[Direction.values().length][Direction.values().length];

    static {
        for (int blockFacing = 0; blockFacing < Direction.values().length; blockFacing++) {
            for (int side = 0; side < Direction.values().length; side++) {
                baseOrientations[blockFacing][side] = getBaseOrientation(Direction.values()[side], Direction.values()[blockFacing]);
            }
        }
    }

    /**
     * Retrieves an empty Gas Tank.
     *
     * @return empty gas tank
     */
    public static ItemStack getEmptyGasTank(GasTankTier tier) {
        switch (tier) {
            case BASIC:
                return MekanismBlock.BASIC_GAS_TANK.getItemStack();
            case ADVANCED:
                return MekanismBlock.ADVANCED_GAS_TANK.getItemStack();
            case ELITE:
                return MekanismBlock.ELITE_GAS_TANK.getItemStack();
            case ULTIMATE:
                return MekanismBlock.ULTIMATE_GAS_TANK.getItemStack();
            case CREATIVE:
                return MekanismBlock.CREATIVE_GAS_TANK.getItemStack();
        }
        return ItemStack.EMPTY;
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
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            if (tileEntity instanceof IActiveState) {
                return ((IActiveState) tileEntity).getActive();
            }
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

    /**
     * Returns the sides in the modified order relative to the machine-based orientation.
     *
     * @param blockFacing - what orientation the block is facing
     *
     * @return Direction.values(), translated to machine orientation
     */
    public static Direction[] getBaseOrientations(Direction blockFacing) {
        return baseOrientations[blockFacing.ordinal()];
    }

    /**
     * Returns an integer facing that converts a world-based orientation to a machine-based orientation.
     *
     * @param side        - world based
     * @param blockFacing - what orientation the block is facing
     *
     * @return machine orientation
     */
    public static Direction getBaseOrientation(Direction side, Direction blockFacing) {
        if (blockFacing == Direction.DOWN) {
            switch (side) {
                case DOWN:
                    return Direction.NORTH;
                case UP:
                    return Direction.SOUTH;
                case NORTH:
                    return Direction.UP;
                case SOUTH:
                    return Direction.DOWN;
                default:
                    return side;
            }
        } else if (blockFacing == Direction.UP) {
            switch (side) {
                case DOWN:
                    return Direction.SOUTH;
                case UP:
                    return Direction.NORTH;
                case NORTH:
                    return Direction.DOWN;
                case SOUTH:
                    return Direction.UP;
                default:
                    return side;
            }
        } else if (blockFacing == Direction.SOUTH || side.getAxis() == Axis.Y) {
            if (side.getAxis() == Axis.Z) {
                return side.getOpposite();
            }
            return side;
        } else if (blockFacing == Direction.NORTH) {
            if (side.getAxis() == Axis.Z) {
                return side;
            }
            return side.getOpposite();
        } else if (blockFacing == Direction.WEST) {
            if (side.getAxis() == Axis.Z) {
                return getRight(side);
            }
            return getLeft(side);
        } else if (blockFacing == Direction.EAST) {
            if (side.getAxis() == Axis.Z) {
                return getLeft(side);
            }
            return getRight(side);
        }
        return side;
    }

    /**
     * Increments the output type of a machine's side.
     *
     * @param config    - configurable machine
     * @param type      - the TransmissionType to modify
     * @param direction - side to increment output of
     */
    public static void incrementOutput(ISideConfiguration config, TransmissionType type, Direction direction) {
        ArrayList<SideData> outputs = config.getConfig().getOutputs(type);
        SideConfig sideConfig = config.getConfig().getConfig(type);
        int max = outputs.size() - 1;
        int current = outputs.indexOf(outputs.get(sideConfig.get(direction)));
        if (current < max) {
            sideConfig.set(direction, (byte) (current + 1));
        } else if (current == max) {
            sideConfig.set(direction, (byte) 0);
        }
        assert config instanceof TileEntity;
        TileEntity tile = (TileEntity) config;
        tile.markDirty();
    }

    /**
     * Decrements the output type of a machine's side.
     *
     * @param config    - configurable machine
     * @param type      - the TransmissionType to modify
     * @param direction - side to increment output of
     */
    public static void decrementOutput(ISideConfiguration config, TransmissionType type, Direction direction) {
        ArrayList<SideData> outputs = config.getConfig().getOutputs(type);
        SideConfig sideConfig = config.getConfig().getConfig(type);
        int max = outputs.size() - 1;
        int current = outputs.indexOf(outputs.get(sideConfig.get(direction)));
        if (current > 0) {
            sideConfig.set(direction, (byte) (current - 1));
        } else if (current == 0) {
            sideConfig.set(direction, (byte) max);
        }
        assert config instanceof TileEntity;
        TileEntity tile = (TileEntity) config;
        tile.markDirty();
    }

    public static float fractionUpgrades(IUpgradeTile mgmt, Upgrade type) {
        return (float) mgmt.getComponent().getUpgrades(type) / (float) type.getMax();
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
        return (int) (def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), -fractionUpgrades(mgmt, Upgrade.SPEED)));
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
        return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2 * fractionUpgrades(mgmt, Upgrade.SPEED) - fractionUpgrades(mgmt, Upgrade.ENERGY));
    }

    /**
     * Gets the energy required per tick for a machine via it's upgrades, not taking into account speed upgrades.
     *
     * @param mgmt - tile containing upgrades
     * @param def  - the original, default energy required
     *
     * @return required energy per tick
     */
    public static double getBaseEnergyPerTick(IUpgradeTile mgmt, double def) {
        return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), -fractionUpgrades(mgmt, Upgrade.ENERGY));
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
        if (mgmt.getComponent().supports(Upgrade.GAS)) {
            return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 2 * fractionUpgrades(mgmt, Upgrade.SPEED) - fractionUpgrades(mgmt, Upgrade.GAS));
        }
        return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(mgmt, Upgrade.SPEED));
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
        return def * Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), fractionUpgrades(mgmt, Upgrade.ENERGY));
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
     * @param coord - the coordinate of the block performing the check
     *
     * @return if the block is indirectly getting powered by LOADED chunks
     */
    public static boolean isGettingPowered(World world, Coord4D coord) {
        for (Direction side : Direction.values()) {
            Coord4D sideCoord = coord.offset(side);
            if (sideCoord.exists(world) && sideCoord.offset(side).exists(world)) {
                BlockState blockState = sideCoord.getBlockState(world);
                boolean weakPower = blockState.getBlock().shouldCheckWeakPower(blockState, world, coord.getPos(), side);
                if (weakPower && isDirectlyGettingPowered(world, sideCoord)) {
                    return true;
                } else if (!weakPower && blockState.getWeakPower(world, sideCoord.getPos(), side) > 0) {
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
     * @param coord - the Coord4D of the block to check
     *
     * @return if the block is directly getting powered
     */
    public static boolean isDirectlyGettingPowered(World world, Coord4D coord) {
        for (Direction side : Direction.values()) {
            Coord4D sideCoord = coord.offset(side);
            if (sideCoord.exists(world)) {
                if (world.getRedstonePower(coord.getPos(), side) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a block is valid for a position and the current block there can be replaced.
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
        BlockState state = coord.getBlockState(world);
        for (Direction dir : Direction.values()) {
            Coord4D offset = coord.offset(dir);
            if (offset.exists(world)) {
                notifyNeighborofChange(world, offset, coord.getPos());
                if (offset.getBlockState(world).isNormalCube(world, offset.getPos())) {
                    offset = offset.offset(dir);
                    if (offset.exists(world)) {
                        Block block1 = offset.getBlock(world);
                        //TODO: Make sure this is passing the correct state
                        if (block1.getWeakChanges(state, world, offset.getPos())) {
                            block1.onNeighborChange(state, world, offset.getPos(), coord.getPos());
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
     * @param coord   neighbor to notify
     * @param fromPos pos of our block that updated
     */
    public static void notifyNeighborofChange(World world, Coord4D coord, BlockPos fromPos) {
        BlockState state = coord.getBlockState(world);
        state.getBlock().onNeighborChange(state, world, coord.getPos(), fromPos);
        //TODO: Check if this should be true for moving
        state.neighborChanged(world, coord.getPos(), world.getBlockState(fromPos).getBlock(), fromPos, false);
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
     * @param orig             - original block
     */
    public static void makeBoundingBlock(World world, BlockPos boundingLocation, Coord4D orig) {
        world.setBlockState(boundingLocation, MekanismBlock.BOUNDING_BLOCK.getBlock().getDefaultState());
        if (!world.isRemote) {
            //TODO: Figure out how to delay this so that the tile gets created before this code runs
            TileEntity tile = getTileEntity(world, boundingLocation);
            if (tile instanceof TileEntityBoundingBlock) {
                ((TileEntityBoundingBlock) tile).setMainLocation(orig.getPos());
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
     * @param orig             - original block
     */
    public static void makeAdvancedBoundingBlock(World world, BlockPos boundingLocation, Coord4D orig) {
        world.setBlockState(boundingLocation, MekanismBlock.ADVANCED_BOUNDING_BLOCK.getBlock().getDefaultState());
        if (!world.isRemote) {
            //TODO: Figure out how to delay this so that the tile gets created before this code runs
            TileEntity tile = getTileEntity(world, boundingLocation);
            if (tile instanceof TileEntityAdvancedBoundingBlock) {
                ((TileEntityAdvancedBoundingBlock) tile).setMainLocation(orig.getPos());
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
    public static void updateBlock(World world, BlockPos pos) {
        if (!world.isBlockLoaded(pos)) {
            return;
        }
        //Schedule a render update regardless of it is an IActiveState with IActiveState#renderUpdate() as true
        // This is because that is mainly used for rendering machine effects, but we need to run a render update
        // anyways here in case IActiveState#renderUpdate() is false and we just had the block rotate.
        // For example the laser, or charge pad.
        //TODO: Render update
        //world.markBlockRangeForRenderUpdate(pos, pos);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof IActiveState) || ((IActiveState) tileEntity).lightUpdate() && MekanismConfig.client.machineEffects.get()) {
            updateAllLightTypes(world, pos);
        }
    }

    /**
     * Updates all light types at the given coordinates.
     *
     * @param world - the world to perform the lighting update in
     * @param pos   - coordinates of the block to update
     */
    public static void updateAllLightTypes(World world, BlockPos pos) {
        //TODO: Update light types
        //world.checkLightFor(LightType.BLOCK, pos);
        //world.checkLightFor(LightType.SKY, pos);
    }

    /**
     * Whether or not a certain block is considered a fluid.
     *
     * @param world - world the block is in
     * @param pos   - coordinates
     *
     * @return if the block is a fluid
     */
    public static boolean isFluid(World world, Coord4D pos) {
        return getFluid(world, pos, false) != null;
    }

    /**
     * Gets a fluid from a certain location.
     *
     * @param world - world the block is in
     * @param pos   - location of the block
     *
     * @return the fluid at the certain location, null if it doesn't exist
     */
    public static FluidStack getFluid(World world, Coord4D pos, boolean filter) {
        BlockState state = pos.getBlockState(world);
        Block block = state.getBlock();
        if (block == Blocks.WATER && state.get(FlowingFluidBlock.LEVEL) == 0) {
            if (!filter) {
                return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
            }
            return new FluidStack(MekanismFluids.HeavyWater, 10);
        } else if (block == Blocks.LAVA && state.get(FlowingFluidBlock.LEVEL) == 0) {
            return new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME);
        } else if (block instanceof IFluidBlock) {
            IFluidBlock fluid = (IFluidBlock) block;
            if (state.getProperties().contains(FlowingFluidBlock.LEVEL) && state.get(FlowingFluidBlock.LEVEL) == 0) {
                return fluid.drain(world, pos.getPos(), false);
            }
        }
        return null;
    }

    /**
     * Whether or not a block is a dead fluid.
     *
     * @param world - world the block is in
     * @param pos   - coordinates
     *
     * @return if the block is a dead fluid
     */
    public static boolean isDeadFluid(World world, Coord4D pos) {
        BlockState state = pos.getBlockState(world);
        Block block = state.getBlock();
        return block instanceof FlowingFluidBlock || block instanceof IFluidBlock;

    }

    /**
     * Gets the flowing block type from a Forge-based fluid. Incorporates the MC system of fliuds as well.
     *
     * @param fluid - the fluid type
     *
     * @return the block corresponding to the given fluid
     */
    public static Block getFlowingBlock(Fluid fluid) {
        if (fluid == null) {
            return null;
        } else if (fluid == FluidRegistry.WATER) {
            return Blocks.WATER;
        } else if (fluid == FluidRegistry.LAVA) {
            return Blocks.LAVA;
        }
        return fluid.getBlock();
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
        //TODO: Move resources for Mekanism Generators/Tools that use this out of Mekanism's resources folder
        return new ResourceLocation(Mekanism.MODID, type.getPrefix() + name);
    }

    /**
     * Marks the chunk this TileEntity is in as modified. Call this method to be sure NBT is written by the defined tile entity.
     *
     * @param tileEntity - TileEntity to save
     */
    public static void saveChunk(TileEntity tileEntity) {
        if (tileEntity == null || tileEntity.isRemoved() || tileEntity.getWorld() == null) {
            return;
        }
        tileEntity.getWorld().markChunkDirty(tileEntity.getPos(), tileEntity);
    }

    /**
     * Whether or not a certain TileEntity can function with redstone logic. Illogical to use unless the defined TileEntity implements IRedstoneControl.
     *
     * @param tileEntity - TileEntity to check
     *
     * @return if the TileEntity can function with redstone logic
     */
    public static boolean canFunction(TileEntity tileEntity) {
        if (!(tileEntity instanceof IRedstoneControl)) {
            return true;
        }
        IRedstoneControl control = (IRedstoneControl) tileEntity;
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
     * @param world  - world the player is in
     * @param player - player to raytrace
     *
     * @return raytraced value
     */
    public static BlockRayTraceResult rayTrace(World world, PlayerEntity player) {
        double reach = Mekanism.proxy.getReach(player);
        Vec3d headVec = getHeadVec(player);
        Vec3d lookVec = player.getLook(1);
        Vec3d endVec = headVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
        //TODO: I am unsure if this is correct so probably needs to be fixed
        RayTraceContext rayTraceContext = new RayTraceContext(headVec, endVec, BlockMode.COLLIDER, FluidMode.NONE, player);
        return world.rayTraceBlocks(rayTraceContext);
    }

    /**
     * Gets the head vector of a player for a ray trace.
     *
     * @param player - player to check
     *
     * @return head location
     */
    private static Vec3d getHeadVec(PlayerEntity player) {
        double posX = player.posX;
        double posY = player.posY;
        double posZ = player.posZ;
        if (!player.world.isRemote) {
            posY += player.getEyeHeight();
            if (player instanceof ServerPlayerEntity && player.isSneaking()) {
                posY -= 0.08;
            }
        }
        return new Vec3d(posX, posY, posZ);
    }

    public static ITextComponent getEnergyDisplayShort(double energy) {
        switch (MekanismConfig.general.energyUnit.get()) {
            case J:
                return UnitDisplayUtils.getDisplayShort(energy, ElectricUnit.JOULES);
            case FE:
                return UnitDisplayUtils.getDisplayShort(ForgeEnergyIntegration.toForge(energy), ElectricUnit.FORGE_ENERGY);
            case EU:
                return UnitDisplayUtils.getDisplayShort(IC2Integration.toEU(energy), ElectricUnit.ELECTRICAL_UNITS);
        }
        return TextComponentUtil.translate("mekanism.error");
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
        return TextComponentUtil.translate("mekanism.error");
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
     * @param obj - coordinate to check
     *
     * @return coordinate display
     */
    public static String getCoordDisplay(Coord4D obj) {
        return "[" + obj.x + ", " + obj.y + ", " + obj.z + "]";
    }

    /**
     * Creates and returns a full gas tank with the specified gas type.
     *
     * @param gas - gas to fill the tank with
     *
     * @return filled gas tank
     */
    public static ItemStack getFullGasTank(GasTankTier tier, Gas gas) {
        ItemStack tank = getEmptyGasTank(tier);
        ItemBlockGasTank item = (ItemBlockGasTank) tank.getItem();
        item.setGas(tank, new GasStack(gas, item.MAX_GAS));
        return tank;
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
    public static boolean isChunkVibrated(Chunk3D chunk) {
        for (Coord4D coord : Mekanism.activeVibrators) {
            if (coord.getChunk3D().equals(chunk)) {
                return true;
            }
        }
        return false;
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

    public static boolean isBCWrench(Item tool) {
        return existsAndInstance(tool, "buildcraft.api.tools.IToolWrench");
    }

    public static boolean isCoFHHammer(Item tool) {
        return existsAndInstance(tool, "cofh.api.item.IToolHammer");
    }

    /**
     * Whether or not the player has a usable wrench for a block at the coordinates given.
     *
     * @param player - the player using the wrench
     * @param pos    - the coordinate of the block being wrenched
     *
     * @return if the player can use the wrench
     *
     * @deprecated use {@link mekanism.common.integration.wrenches.Wrenches#getHandler(ItemStack)}
     */
    @Deprecated
    public static boolean hasUsableWrench(PlayerEntity player, BlockPos pos) {
        ItemStack tool = player.inventory.getCurrentItem();
        if (tool.isEmpty()) {
            return false;
        }
        if (tool.getItem() instanceof IMekWrench && ((IMekWrench) tool.getItem()).canUseWrench(tool, player, pos)) {
            return true;
        }
        try {
            if (isBCWrench(tool.getItem())) { //TODO too much hassle to check BC wrench-ability
                return true;
            }
            if (isCoFHHammer(tool.getItem())) { // TODO Implement CoFH Hammer && ((IToolHammer)tool.getItem()).isUsable(tool, player, pos))
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Nonnull
    public static String getLastKnownUsername(UUID uuid) {
        String ret = UsernameCache.getLastKnownUsername(uuid);
        if (ret == null && !warnedFails.contains(uuid) && EffectiveSide.get() == LogicalSide.SERVER) { // see if MC/Yggdrasil knows about it?!
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

    public static TileEntity getTileEntitySafe(IBlockReader worldIn, BlockPos pos) {
        //TODO: Remove this/replace with getTileEntity
        return worldIn.getTileEntity(pos);//worldIn instanceof Region ? ((Region) worldIn).getTileEntity(pos, Chunk.CreateEntityType.CHECK) : worldIn.getTileEntity(pos);
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
    public static TileEntity getTileEntity(IWorldReader world, BlockPos pos) {
        if (world != null && world.isBlockLoaded(pos)) {
            return world.getTileEntity(pos);
        }
        return null;
    }

    /**
     * Dismantles a block, dropping it and removing it from the world.
     */
    public static void dismantleBlock(BlockState state, World world, BlockPos pos) {
        dismantleBlock(state, world, pos, world.getTileEntity(pos));
    }

    public static void dismantleBlock(BlockState state, World world, BlockPos pos, TileEntity tile) {
        Block.spawnDrops(state, world, pos, tile);
        world.removeBlock(pos, false);
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
        GUI_ELEMENT("gui/elements"),
        SOUND("sound"),
        RENDER("render"),
        TEXTURE_BLOCKS("textures/blocks"),
        TEXTURE_ITEMS("textures/items"),
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