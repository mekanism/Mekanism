package mekanism.common.util;

import com.mojang.authlib.GameProfile;
import ic2.api.energy.EnergyNet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.IMekWrench;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismItems;
import mekanism.common.OreDictCache;
import mekanism.common.SideData;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.BinTier;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.Tier.InductionCellTier;
import mekanism.common.Tier.InductionProviderTier;
import mekanism.common.Upgrade;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.config.MekanismConfig.client;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.inventory.InventoryPersonalChest;
import mekanism.common.inventory.container.ContainerPersonalChest;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemBlockTransmitter;
import mekanism.common.network.PacketPersonalChest.PersonalChestMessage;
import mekanism.common.network.PacketPersonalChest.PersonalChestPacketType;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.component.SideConfig;
import mekanism.common.util.UnitDisplayUtils.ElectricUnit;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Utilities used by Mekanism. All miscellaneous methods are located here.
 *
 * @author AidanBrady
 */
public final class MekanismUtils {

    public static final EnumFacing[] SIDE_DIRS = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST,
          EnumFacing.EAST};

    public static final Map<String, Class<?>> classesFound = new HashMap<>();

    private static final List<UUID> warnedFails = new ArrayList<>();
    /**
     * Pre-calculated cache of translated block orientations
     */
    private static final EnumFacing[][] baseOrientations = new EnumFacing[EnumFacing.VALUES.length][EnumFacing.VALUES.length];

    static {
        for (int blockFacing = 0; blockFacing < EnumFacing.VALUES.length; blockFacing++) {
            for (int side = 0; side < EnumFacing.VALUES.length; side++) {
                baseOrientations[blockFacing][side] = getBaseOrientation(EnumFacing.VALUES[side],
                      EnumFacing.VALUES[blockFacing]);
            }
        }
    }


    public static String merge(List<String> text) {
        StringBuilder builder = new StringBuilder();

        for (String s : text) {
            builder.append(s);
        }

        return builder.toString();
    }

    /**
     * Copies an ItemStack and returns it with a defined getCount().
     *
     * @param itemstack - stack to change size
     * @param size - size to change to
     * @return resized ItemStack
     */
    public static ItemStack size(ItemStack itemstack, int size) {
        ItemStack newStack = itemstack.copy();
        newStack.setCount(size);
        return newStack;
    }

    /**
     * Adds a recipe directly to the CraftingManager that works with the Forge Ore Dictionary.
     *
     * @param output the ItemStack produced by this recipe
     * @param params the items/blocks/itemstacks required to create the output ItemStack
     */
    public static void addRecipe(ItemStack output, Object[] params) {
//		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(output, params));
    }

    /**
     * Retrieves an empty Energy Cube with a defined tier.
     *
     * @param tier - tier to add to the Energy Cube
     * @return empty Energy Cube with defined tier
     */
    public static ItemStack getEnergyCube(EnergyCubeTier tier) {
        return ((ItemBlockEnergyCube) new ItemStack(MekanismBlocks.EnergyCube).getItem()).getUnchargedItem(tier);
    }

    /**
     * Returns a Control Circuit with a defined tier, using an OreDict value if enabled in the config.
     *
     * @param tier - tier to add to the Control Circuit
     * @return Control Circuit with defined tier
     */
    public static Object getControlCircuit(BaseTier tier) {
        return general.controlCircuitOreDict ? "circuit" + tier.getSimpleName()
              : new ItemStack(MekanismItems.ControlCircuit, 1, tier.ordinal());
    }

    /**
     * Retrieves an empty Induction Cell with a defined tier.
     *
     * @param tier - tier to add to the Induction Cell
     * @return empty Induction Cell with defined tier
     */
    public static ItemStack getInductionCell(InductionCellTier tier) {
        return ((ItemBlockBasic) new ItemStack(MekanismBlocks.BasicBlock2, 1, 3).getItem()).getUnchargedCell(tier);
    }

    /**
     * Retrieves an Induction Provider with a defined tier.
     *
     * @param tier - tier to add to the Induction Provider
     * @return Induction Provider with defined tier
     */
    public static ItemStack getInductionProvider(InductionProviderTier tier) {
        return ((ItemBlockBasic) new ItemStack(MekanismBlocks.BasicBlock2, 1, 4).getItem()).getUnchargedProvider(tier);
    }

    /**
     * Retrieves an Bin with a defined tier.
     *
     * @param tier - tier to add to the Bin
     * @return Bin with defined tier
     */
    public static ItemStack getBin(BinTier tier) {
        ItemStack ret = new ItemStack(MekanismBlocks.BasicBlock, 1, 6);
        ((ItemBlockBasic) ret.getItem()).setBaseTier(ret, tier.getBaseTier());

        return ret;
    }

    /**
     * Retrieves an empty Gas Tank.
     *
     * @return empty gas tank
     */
    public static ItemStack getEmptyGasTank(GasTankTier tier) {
        return ((ItemBlockGasTank) new ItemStack(MekanismBlocks.GasTank).getItem()).getEmptyItem(tier);
    }

    public static ItemStack getEmptyFluidTank(FluidTankTier tier) {
        ItemStack stack = new ItemStack(MekanismBlocks.MachineBlock2, 1, 11);
        ItemBlockMachine itemMachine = (ItemBlockMachine) stack.getItem();
        itemMachine.setBaseTier(stack, tier.getBaseTier());

        return stack;
    }

    public static ItemStack getTransmitter(TransmitterType type, BaseTier tier, int amount) {
        ItemStack stack = new ItemStack(MekanismBlocks.Transmitter, amount, type.ordinal());
        ItemBlockTransmitter itemTransmitter = (ItemBlockTransmitter) stack.getItem();
        itemTransmitter.setBaseTier(stack, tier);

        return stack;
    }

    /**
     * Retrieves a Factory with a defined tier and recipe type.
     *
     * @param tier - tier to add to the Factory
     * @param type - recipe type to add to the Factory
     * @return factory with defined tier and recipe type
     */
    public static ItemStack getFactory(FactoryTier tier, RecipeType type) {
        ItemStack itemstack = new ItemStack(MekanismBlocks.MachineBlock, 1,
              MachineType.BASIC_FACTORY.ordinal() + tier.ordinal());
        ((IFactory) itemstack.getItem()).setRecipeType(type.ordinal(), itemstack);
        return itemstack;
    }

    /**
     * Checks if a machine is in it's active state.
     *
     * @param world World of the machine to check
     * @param pos The position of the machine
     * @return if machine is active
     */
    public static boolean isActive(IBlockAccess world, BlockPos pos) {
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
     * @return left side
     */
    public static EnumFacing getLeft(EnumFacing orientation) {
        return orientation.rotateY();
    }

    /**
     * Gets the right side of a certain orientation.
     *
     * @param orientation Current orientation of the machine
     * @return right side
     */
    public static EnumFacing getRight(EnumFacing orientation) {
        return orientation.rotateYCCW();
    }

    /**
     * Gets the opposite side of a certain orientation.
     *
     * @param orientation Current orientation of the machine
     * @return opposite side
     */
    public static EnumFacing getBack(EnumFacing orientation) {
        return orientation.getOpposite();
    }

    /**
     * Checks to see if a specified ItemStack is stored in the Ore Dictionary with the specified name.
     *
     * @param check - ItemStack to check
     * @param oreDict - name to check with
     * @return if the ItemStack has the Ore Dictionary key
     */
    public static boolean oreDictCheck(ItemStack check, String oreDict) {
        boolean hasResource = false;

        for (ItemStack ore : OreDictionary.getOres(oreDict)) {
            if (ore.isItemEqual(check)) {
                hasResource = true;
            }
        }

        return hasResource;
    }

    /**
     * Gets the ore dictionary name of a defined ItemStack.
     *
     * @param check - ItemStack to check OreDict name of
     * @return OreDict name
     */
    public static List<String> getOreDictName(ItemStack check) {
        return OreDictCache.getOreDictName(check);
    }

    /**
     * Returns the sides in the modified order relative to the machine-based orientation.
     *
     * @param blockFacing - what orientation the block is facing
     * @return EnumFacing.VALUES, translated to machine orientation
     */
    public static EnumFacing[] getBaseOrientations(EnumFacing blockFacing) {
        return baseOrientations[blockFacing.ordinal()];
    }

    /**
     * Returns an integer facing that converts a world-based orientation to a machine-based orientation.
     *
     * @param side - world based
     * @param blockFacing - what orientation the block is facing
     * @return machine orientation
     */
    public static EnumFacing getBaseOrientation(EnumFacing side, EnumFacing blockFacing) {
        if (blockFacing == EnumFacing.DOWN) {
            switch (side) {
                case DOWN:
                    return EnumFacing.NORTH;
                case UP:
                    return EnumFacing.SOUTH;
                case NORTH:
                    return EnumFacing.UP;
                case SOUTH:
                    return EnumFacing.DOWN;
                default:
                    return side;
            }
        } else if (blockFacing == EnumFacing.UP) {
            switch (side) {
                case DOWN:
                    return EnumFacing.SOUTH;
                case UP:
                    return EnumFacing.NORTH;
                case NORTH:
                    return EnumFacing.DOWN;
                case SOUTH:
                    return EnumFacing.UP;
                default:
                    return side;
            }
        } else if (blockFacing == EnumFacing.SOUTH || side.getAxis() == Axis.Y) {
            if (side.getAxis() == Axis.Z) {
                return side.getOpposite();
            }

            return side;
        } else if (blockFacing == EnumFacing.NORTH) {
            if (side.getAxis() == Axis.Z) {
                return side;
            }

            return side.getOpposite();
        } else if (blockFacing == EnumFacing.WEST) {
            if (side.getAxis() == Axis.Z) {
                return getRight(side);
            }

            return getLeft(side);
        } else if (blockFacing == EnumFacing.EAST) {
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
     * @param config - configurable machine
     * @param type - the TransmissionType to modify
     * @param direction - side to increment output of
     */
    public static void incrementOutput(ISideConfiguration config, TransmissionType type, EnumFacing direction) {
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
     * @param config - configurable machine
     * @param type - the TransmissionType to modify
     * @param direction - side to increment output of
     */
    public static void decrementOutput(ISideConfiguration config, TransmissionType type, EnumFacing direction) {
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
     * @param def - the original, default ticks required
     * @return required operating ticks
     */
    public static int getTicks(IUpgradeTile mgmt, int def) {
        return (int) (def * Math.pow(general.maxUpgradeMultiplier, -fractionUpgrades(mgmt, Upgrade.SPEED)));
    }

    /**
     * Gets the energy required per tick for a machine via it's upgrades.
     *
     * @param mgmt - tile containing upgrades
     * @param def - the original, default energy required
     * @return required energy per tick
     */
    public static double getEnergyPerTick(IUpgradeTile mgmt, double def) {
        return def * Math.pow(general.maxUpgradeMultiplier,
              2 * fractionUpgrades(mgmt, Upgrade.SPEED) - fractionUpgrades(mgmt, Upgrade.ENERGY));
    }

    /**
     * Gets the energy required per tick for a machine via it's upgrades, not taking into account speed upgrades.
     *
     * @param mgmt - tile containing upgrades
     * @param def - the original, default energy required
     * @return required energy per tick
     */
    public static double getBaseEnergyPerTick(IUpgradeTile mgmt, double def) {
        return def * Math.pow(general.maxUpgradeMultiplier, -fractionUpgrades(mgmt, Upgrade.ENERGY));
    }

    /**
     * Gets the secondary energy required per tick for a machine via upgrades.
     *
     * @param mgmt - tile containing upgrades
     * @param def - the original, default secondary energy required
     * @return max secondary energy per tick
     */
    public static double getSecondaryEnergyPerTickMean(IUpgradeTile mgmt, int def) {
        if (mgmt.getComponent().supports(Upgrade.GAS)) {
            return def * Math.pow(general.maxUpgradeMultiplier,
                  2 * fractionUpgrades(mgmt, Upgrade.SPEED) - fractionUpgrades(mgmt, Upgrade.GAS));
        }

        return def * Math.pow(general.maxUpgradeMultiplier, fractionUpgrades(mgmt, Upgrade.SPEED));
    }

    /**
     * Gets the maximum energy for a machine via it's upgrades.
     *
     * @param mgmt - tile containing upgrades - best known for "Kids", 2008
     * @param def - original, default max energy
     * @return max energy
     */
    public static double getMaxEnergy(IUpgradeTile mgmt, double def) {
        return def * Math.pow(general.maxUpgradeMultiplier, fractionUpgrades(mgmt, Upgrade.ENERGY));
    }

    /**
     * Gets the maximum energy for a machine's item form via it's upgrades.
     *
     * @param itemStack - stack holding energy upgrades
     * @param def - original, default max energy
     * @return max energy
     */
    public static double getMaxEnergy(ItemStack itemStack, double def) {
        Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(itemStack));
        float numUpgrades = upgrades.get(Upgrade.ENERGY) == null ? 0 : (float) upgrades.get(Upgrade.ENERGY);
        return def * Math.pow(general.maxUpgradeMultiplier, numUpgrades / (float) Upgrade.ENERGY.getMax());
    }

    /**
     * Better version of the World.getRedstonePowerFromNeighbors() method that doesn't load chunks.
     *
     * @param world - the world to perform the check in
     * @param coord - the coordinate of the block performing the check
     * @return if the block is indirectly getting powered by LOADED chunks
     */
    public static boolean isGettingPowered(World world, Coord4D coord) {
        for (EnumFacing side : EnumFacing.VALUES) {
            Coord4D sideCoord = coord.offset(side);

            if (sideCoord.exists(world) && sideCoord.offset(side).exists(world)) {
                IBlockState blockState = sideCoord.getBlockState(world);
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
     * @return if the block is directly getting powered
     */
    public static boolean isDirectlyGettingPowered(World world, Coord4D coord) {
        for (EnumFacing side : EnumFacing.VALUES) {
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
     * Notifies neighboring blocks of a TileEntity change without loading chunks.
     *
     * @param world - world to perform the operation in
     * @param coord - Coord4D to perform the operation on
     */
    public static void notifyLoadedNeighborsOfTileChange(World world, Coord4D coord) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            Coord4D offset = coord.offset(dir);

            if (offset.exists(world)) {
                notifyNeighborofChange(world, offset, coord.getPos());

                if (offset.getBlockState(world).isNormalCube()) {
                    offset = offset.offset(dir);

                    if (offset.exists(world)) {
                        Block block1 = offset.getBlock(world);

                        if (block1.getWeakChanges(world, offset.getPos())) {
                            block1.onNeighborChange(world, offset.getPos(), coord.getPos());
                        }
                    }
                }
            }
        }
    }

    /**
     * Calls BOTH neighbour changed functions because nobody can decide on which one to implement.
     *
     * @param world world the change exists in
     * @param coord neighbor to notify
     * @param fromPos pos of our block that updated
     */
    public static void notifyNeighborofChange(World world, Coord4D coord, BlockPos fromPos) {
        IBlockState state = coord.getBlockState(world);
        state.getBlock().onNeighborChange(world, coord.getPos(), fromPos);
        state.neighborChanged(world, coord.getPos(), world.getBlockState(fromPos).getBlock(), fromPos);
    }

    /**
     * Places a fake bounding block at the defined location.
     *
     * @param world - world to place block in
     * @param boundingLocation - coordinates of bounding block
     * @param orig - original block
     */
    public static void makeBoundingBlock(World world, BlockPos boundingLocation, Coord4D orig) {
        world.setBlockState(boundingLocation, MekanismBlocks.BoundingBlock.getStateFromMeta(0));

        if (!world.isRemote) {
            ((TileEntityBoundingBlock) world.getTileEntity(boundingLocation)).setMainLocation(orig.getPos());
        }
    }

    /**
     * Places a fake advanced bounding block at the defined location.
     *
     * @param world - world to place block in
     * @param boundingLocation - coordinates of bounding block
     * @param orig - original block
     */
    public static void makeAdvancedBoundingBlock(World world, BlockPos boundingLocation, Coord4D orig) {
        world.setBlockState(boundingLocation, MekanismBlocks.BoundingBlock.getStateFromMeta(1));

        if (!world.isRemote) {
            ((TileEntityAdvancedBoundingBlock) world.getTileEntity(boundingLocation)).setMainLocation(orig.getPos());
        }
    }

    /**
     * Updates a block's light value and marks it for a render update.
     *
     * @param world - world the block is in
     * @param pos Position of the block
     */
    public static void updateBlock(World world, BlockPos pos) {
        if (!(world.getTileEntity(pos) instanceof IActiveState) || ((IActiveState) world.getTileEntity(pos))
              .renderUpdate()) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }

        if (!(world.getTileEntity(pos) instanceof IActiveState)
              || ((IActiveState) world.getTileEntity(pos)).lightUpdate() && client.machineEffects) {
            updateAllLightTypes(world, pos);
        }
    }

    /**
     * Updates all light types at the given coordinates.
     *
     * @param world - the world to perform the lighting update in
     * @param pos - coordinates of the block to update
     */
    public static void updateAllLightTypes(World world, BlockPos pos) {
        world.checkLightFor(EnumSkyBlock.BLOCK, pos);
        world.checkLightFor(EnumSkyBlock.SKY, pos);
    }

    /**
     * Whether or not a certain block is considered a fluid.
     *
     * @param world - world the block is in
     * @param pos - coordinates
     * @return if the block is a fluid
     */
    public static boolean isFluid(World world, Coord4D pos) {
        return getFluid(world, pos, false) != null;
    }

    /**
     * Gets a fluid from a certain location.
     *
     * @param world - world the block is in
     * @param pos - location of the block
     * @return the fluid at the certain location, null if it doesn't exist
     */
    public static FluidStack getFluid(World world, Coord4D pos, boolean filter) {
        IBlockState state = pos.getBlockState(world);
        Block block = state.getBlock();

        if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && state.getValue(BlockLiquid.LEVEL) == 0) {
            if (!filter) {
                return new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
            } else {
                return new FluidStack(MekanismFluids.HeavyWater, 10);
            }
        } else if ((block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) && state.getValue(BlockLiquid.LEVEL) == 0) {
            return new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME);
        } else if (block instanceof IFluidBlock) {
            IFluidBlock fluid = (IFluidBlock) block;

            if (state.getProperties().containsKey(BlockFluidBase.LEVEL) && state.getValue(BlockFluidBase.LEVEL) == 0) {
                return fluid.drain(world, pos.getPos(), false);
            }
        }

        return null;
    }

    /**
     * Whether or not a block is a dead fluid.
     *
     * @param world - world the block is in
     * @param pos - coordinates
     * @return if the block is a dead fluid
     */
    public static boolean isDeadFluid(World world, Coord4D pos) {
        IBlockState state = pos.getBlockState(world);
        Block block = state.getBlock();

        if (block.getMetaFromState(state) == 0) {
            return false;
        }

        return block instanceof BlockLiquid || block instanceof IFluidBlock;

    }

    /**
     * Gets the flowing block type from a Forge-based fluid. Incorporates the MC system of fliuds as well.
     *
     * @param fluid - the fluid type
     * @return the block corresponding to the given fluid
     */
    public static Block getFlowingBlock(Fluid fluid) {
        if (fluid == null) {
            return null;
        } else if (fluid == FluidRegistry.WATER) {
            return Blocks.FLOWING_WATER;
        } else if (fluid == FluidRegistry.LAVA) {
            return Blocks.FLOWING_LAVA;
        } else {
            return fluid.getBlock();
        }
    }

    /**
     * FML doesn't really do GUIs the way it's supposed to -- opens Electric Chest GUI on client and server. Call this
     * method server-side only!
     *
     * @param player - player to open GUI
     * @param tileEntity - TileEntity of the chest, if it's not an item
     * @param inventory - IInventory of the item, if it's not a block
     * @param isBlock - whether or not this electric chest is in it's block form
     */
    public static void openPersonalChestGui(EntityPlayerMP player, TileEntityPersonalChest tileEntity,
          InventoryPersonalChest inventory, boolean isBlock) {
        player.getNextWindowId();
        player.closeContainer();
        int id = player.currentWindowId;

        if (isBlock) {
            Mekanism.packetHandler.sendTo(
                  new PersonalChestMessage(PersonalChestPacketType.CLIENT_OPEN, true, 0, id, Coord4D.get(tileEntity),
                        null), player);
        } else {
            Mekanism.packetHandler.sendTo(
                  new PersonalChestMessage(PersonalChestPacketType.CLIENT_OPEN, false, 0, id, null,
                        inventory.currentHand), player);
        }

        player.openContainer = new ContainerPersonalChest(player.inventory, tileEntity, inventory, isBlock);
        player.openContainer.windowId = id;
        player.openContainer.addListener(player);
    }

    /**
     * Gets a ResourceLocation with a defined resource type and name.
     *
     * @param type - type of resource to retrieve
     * @param name - simple name of file to retrieve as a ResourceLocation
     * @return the corresponding ResourceLocation
     */
    public static ResourceLocation getResource(ResourceType type, String name) {
        return new ResourceLocation("mekanism", type.getPrefix() + name);
    }

    /**
     * Removes all recipes that are used to create the defined ItemStacks.
     *
     * @param itemStacks - ItemStacks to perform the operation on
     * @return if any recipes were removed
     */
    public static boolean removeRecipes(ItemStack... itemStacks) {
        boolean didRemove = false;

//		for(Iterator itr = CraftingManager.getInstance().getRecipeList().iterator(); itr.hasNext();)
//		{
//			Object obj = itr.next();
//
//			if(obj instanceof IRecipe && ((IRecipe)obj).getRecipeOutput() != null)
//			{
//				for(ItemStack itemStack : itemStacks)
//				{
//					if(((IRecipe)obj).getRecipeOutput().isItemEqual(itemStack))
//					{
//						itr.remove();
//						didRemove = true;
//						break;
//					}
//				}
//			}
//		}

        return didRemove;
    }

    /**
     * Marks the chunk this TileEntity is in as modified. Call this method to be sure NBT is written by the defined tile
     * entity.
     *
     * @param tileEntity - TileEntity to save
     */
    public static void saveChunk(TileEntity tileEntity) {
        if (tileEntity == null || tileEntity.isInvalid() || tileEntity.getWorld() == null) {
            return;
        }

        tileEntity.getWorld().markChunkDirty(tileEntity.getPos(), tileEntity);
    }

    /**
     * Whether or not a certain TileEntity can function with redstone logic. Illogical to use unless the defined
     * TileEntity implements IRedstoneControl.
     *
     * @param tileEntity - TileEntity to check
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
     * @param world - world the player is in
     * @param player - player to raytrace
     * @return raytraced value
     */
    public static RayTraceResult rayTrace(World world, EntityPlayer player) {
        double reach = Mekanism.proxy.getReach(player);

        Vec3d headVec = getHeadVec(player);
        Vec3d lookVec = player.getLook(1);
        Vec3d endVec = headVec.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);

        return world.rayTraceBlocks(headVec, endVec, true);
    }

    /**
     * Gets the head vector of a player for a ray trace.
     *
     * @param player - player to check
     * @return head location
     */
    private static Vec3d getHeadVec(EntityPlayer player) {
        double posX = player.posX;
        double posY = player.posY;
        double posZ = player.posZ;

        if (!player.world.isRemote) {
            posY += player.getEyeHeight();

            if (player instanceof EntityPlayerMP && player.isSneaking()) {
                posY -= 0.08;
            }
        }

        return new Vec3d(posX, posY, posZ);
    }

    /**
     * Gets a rounded energy display of a defined amount of energy.
     *
     * @param energy - energy to display
     * @return rounded energy display
     */
    public static String getEnergyDisplay(double energy) {
        if (energy == Double.MAX_VALUE) {
            return LangUtils.localize("gui.infinite");
        }

        switch (general.energyUnit) {
            case J:
                return UnitDisplayUtils.getDisplayShort(energy, ElectricUnit.JOULES);
            case RF:
                return UnitDisplayUtils.getDisplayShort(energy * general.TO_RF, ElectricUnit.REDSTONE_FLUX);
            case EU:
                return UnitDisplayUtils.getDisplayShort(energy * general.TO_IC2, ElectricUnit.ELECTRICAL_UNITS);
            case T:
                return UnitDisplayUtils.getDisplayShort(energy * general.TO_TESLA, ElectricUnit.TESLA);
        }

        return "error";
    }

    public static String getEnergyDisplay(double energy, double max) {
        if (energy == Double.MAX_VALUE) {
            return LangUtils.localize("gui.infinite");
        }

        String energyString = getEnergyDisplay(energy);
        String maxString = getEnergyDisplay(max);

        return energyString + "/" + maxString;
    }

    /**
     * Convert from the unit defined in the configuration to joules.
     *
     * @param energy - energy to convert
     * @return energy converted to joules
     */
    public static double convertToJoules(double energy) {
        switch (general.energyUnit) {
            case RF:
                return energy * general.FROM_RF;
            case EU:
                return energy * general.FROM_IC2;
            case T:
                return energy * general.FROM_TESLA;
            default:
                return energy;
        }
    }

    /**
     * Convert from joules to the unit defined in the configuration.
     *
     * @param energy - energy to convert
     * @return energy converted to configured unit
     */
    public static double convertToDisplay(double energy) {
        switch (general.energyUnit) {
            case RF:
                return energy * general.TO_RF;
            case EU:
                return energy * general.TO_IC2;
            case T:
                return energy * general.TO_RF / 10;
            default:
                return energy;
        }
    }

    /**
     * Gets a rounded energy display of a defined amount of energy.
     *
     * @param T - temperature to display
     * @return rounded energy display
     */
    public static String getTemperatureDisplay(double T, TemperatureUnit unit) {
        double TK = unit.convertToK(T, true);

        switch (general.tempUnit) {
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

        return "error";
    }

    /**
     * Whether or not IC2 power should be used, taking into account whether or not it is installed or another mod is
     * providing its API.
     *
     * @return if IC2 power should be used
     */
    public static boolean useIC2() {
        return Mekanism.hooks.IC2Loaded && EnergyNet.instance != null && !general.blacklistIC2;
    }

    /**
     * Whether or not RF power should be used.
     *
     * @return if RF power should be used
     */
    public static boolean useRF() {
        return Mekanism.hooks.RFLoaded && !general.blacklistRF;
    }

    /**
     * Whether or not Tesla power should be used.
     *
     * @return if Tesla power should be used
     */
    public static boolean useTesla() {
        return Mekanism.hooks.TeslaLoaded && !general.blacklistTesla;
    }

    /**
     * Whether or not Forge power should be used.
     *
     * @return if Forge power should be used
     */
    public static boolean useForge() {
        return !general.blacklistForge;
    }

    /**
     * Gets a clean view of a coordinate value without the dimension ID.
     *
     * @param obj - coordinate to check
     * @return coordinate display
     */
    public static String getCoordDisplay(Coord4D obj) {
        return "[" + obj.x + ", " + obj.y + ", " + obj.z + "]";
    }

    @SideOnly(Side.CLIENT)
    public static List<String> splitTooltip(String s, ItemStack stack) {
        s = s.trim();

        try {
            FontRenderer renderer = (FontRenderer) Mekanism.proxy.getFontRenderer();

            if (!stack.isEmpty() && stack.getItem().getFontRenderer(stack) != null) {
                renderer = stack.getItem().getFontRenderer(stack);
            }

            List<String> words = new ArrayList<>();
            List<String> lines = new ArrayList<>();

            String currentWord = "";

            for (Character c : s.toCharArray()) {
                if (c.equals(' ')) {
                    words.add(currentWord);
                    currentWord = "";
                } else {
                    currentWord += c;
                }
            }

            if (!currentWord.isEmpty()) {
                words.add(currentWord);
            }

            String currentLine = "";

            for (String word : words) {
                if (currentLine.isEmpty() || renderer.getStringWidth(currentLine + " " + word) <= 200) {
                    if (currentLine.length() > 0) {
                        currentLine += " ";
                    }

                    currentLine += word;
                } else {
                    lines.add(currentLine);
                    currentLine = word;
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine);
            }

            return lines;
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Creates and returns a full gas tank with the specified gas type.
     *
     * @param gas - gas to fill the tank with
     * @return filled gas tank
     */
    public static ItemStack getFullGasTank(GasTankTier tier, Gas gas) {
        ItemStack tank = getEmptyGasTank(tier);
        ItemBlockGasTank item = (ItemBlockGasTank) tank.getItem();
        item.setGas(tank, new GasStack(gas, item.MAX_GAS));

        return tank;
    }

    public static InventoryCrafting getDummyCraftingInv() {
        Container tempContainer = new Container() {
            @Override
            public boolean canInteractWith(@Nonnull EntityPlayer player) {
                return false;
            }
        };

        return new InventoryCrafting(tempContainer, 3, 3);
    }

    /**
     * Finds the output of a defined InventoryCrafting grid.
     *
     * @param inv - InventoryCrafting to check
     * @param world - world reference
     * @return output ItemStack
     */
    public static ItemStack findMatchingRecipe(InventoryCrafting inv, World world) {
        NonNullList<ItemStack> dmgItems = NonNullList.withSize(2, ItemStack.EMPTY);

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                if (dmgItems.get(0).isEmpty()) {
                    dmgItems.set(0, inv.getStackInSlot(i));
                } else {
                    dmgItems.set(1, inv.getStackInSlot(i));
                    break;
                }
            }
        }

        if (dmgItems.get(0).isEmpty()) {
            return ItemStack.EMPTY;
        }

        if ((!dmgItems.get(1).isEmpty()) && (dmgItems.get(0).getItem() == dmgItems.get(1).getItem()) && (
              dmgItems.get(0).getCount() == 1) && (dmgItems.get(1).getCount() == 1) && dmgItems.get(0).getItem()
              .isRepairable()) {
            Item theItem = dmgItems.get(0).getItem();
            int dmgDiff0 = theItem.getMaxDamage() - dmgItems.get(0).getItemDamage();
            int dmgDiff1 = theItem.getMaxDamage() - dmgItems.get(1).getItemDamage();
            int value = dmgDiff0 + dmgDiff1 + theItem.getMaxDamage() * 5 / 100;
            int solve = Math.max(0, theItem.getMaxDamage() - value);

            return new ItemStack(dmgItems.get(0).getItem(), 1, solve);
        }

        IRecipe potentialResult = CraftingManager.findMatchingRecipe(inv, world);
        return potentialResult != null ? potentialResult.getRecipeOutput() : ItemStack.EMPTY;
    }

    /**
     * Whether or not the provided chunk is being vibrated by a Seismic Vibrator.
     *
     * @param chunk - chunk to check
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
     * Whether or not a given EntityPlayer is considered an Op.
     *
     * @param p - player to check
     * @return if the player has operator privileges
     */
    public static boolean isOp(EntityPlayer p) {
        if (!(p instanceof EntityPlayerMP)) {
            return false;
        }

        EntityPlayerMP player = (EntityPlayerMP) p;

        return general.opsBypassRestrictions && player.server.getPlayerList().canSendCommands(player.getGameProfile());
    }

    /**
     * Gets the item ID from a given ItemStack
     *
     * @param itemStack - ItemStack to check
     * @return item ID of the ItemStack
     */
    public static int getID(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return -1;
        }

        return Item.getIdFromItem(itemStack.getItem());
    }

    public static boolean classExists(String className) {
        if (classesFound.containsKey(className)) {
            return classesFound.get(className) != null;
        }

        Class<?> found;

        try {
            found = Class.forName(className);
        } catch (ClassNotFoundException e) {
            found = null;
        }

        classesFound.put(className, found);

        return found != null;
    }

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
     * @param pos - the coordinate of the block being wrenched
     * @return if the player can use the wrench
     * @deprecated use {@link mekanism.common.integration.wrenches.Wrenches#getHandler(ItemStack)}
     */
    @Deprecated
    public static boolean hasUsableWrench(EntityPlayer player, BlockPos pos) {
        ItemStack tool = player.inventory.getCurrentItem();

        if (tool.isEmpty()) {
            return false;
        }

        if (tool.getItem() instanceof IMekWrench && ((IMekWrench) tool.getItem()).canUseWrench(tool, player, pos)) {
            return true;
        }

        try {
            if (isBCWrench(tool.getItem())) //TODO too much hassle to check BC wrench-ability
            {
                return true;
            }

            if (isCoFHHammer(
                  tool.getItem())) // TODO Implement CoFH Hammer && ((IToolHammer)tool.getItem()).isUsable(tool, player, pos))
            {
                return true;
            }
        } catch (Throwable ignored) {
        }

        return false;
    }

    @Nonnull
    public static String getLastKnownUsername(UUID uuid) {
        String ret = UsernameCache.getLastKnownUsername(uuid);

        if (ret == null && !warnedFails.contains(uuid) && FMLCommonHandler.instance().getEffectiveSide()
              == Side.SERVER) { // see if MC/Yggdrasil knows about it?!
            GameProfile gp = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache()
                  .getProfileByUUID(uuid);

            if (gp != null) {
                ret = gp.getName();
            }
        }

        if (ret == null && !warnedFails.contains(uuid)) {
            Mekanism.logger
                  .warn("Failed to retrieve username for UUID {}, you might want to add it to the JSON cache", uuid);
            warnedFails.add(uuid);
        }

        return ret != null ? ret : "<???>";
    }

    public static TileEntity getTileEntitySafe(IBlockAccess worldIn, BlockPos pos) {
        return worldIn instanceof ChunkCache ? ((ChunkCache) worldIn)
              .getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : worldIn.getTileEntity(pos);
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
