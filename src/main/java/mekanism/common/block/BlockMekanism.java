package mekanism.common.block;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.sustained.ISustainedInventory;
import mekanism.api.sustained.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.item.IItemEnergized;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

/**
 * Special handling for block drops that need TileEntity data
 */
public abstract class BlockMekanism extends Block {

    protected BlockMekanism(Block.Properties properties) {
        super(properties);
        setDefaultState(BlockStateHelper.getDefaultState(stateContainer.getBaseState()));
    }

    @Nonnull
    protected ItemStack getDropItem(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        ItemStack itemStack = new ItemStack(this);
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return itemStack;
        }
        //TODO: If crashes happen here because of lack of NBT make things use ItemDataUtils
        Item item = itemStack.getItem();

        //Set any data that is block specific rather than tile specific
        itemStack = setItemData(state, world, pos, tile, itemStack);

        if (item instanceof ISecurityItem && tile.hasSecurity()) {
            ISecurityItem securityItem = (ISecurityItem) item;
            securityItem.setOwnerUUID(itemStack, tile.getSecurity().getOwnerUUID());
            securityItem.setSecurity(itemStack, tile.getSecurity().getMode());
        }
        if (tile.supportsUpgrades()) {
            tile.getComponent().write(ItemDataUtils.getDataMap(itemStack));
        }
        if (tile instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tile;
            config.getConfig().write(ItemDataUtils.getDataMap(itemStack));
            config.getEjector().write(ItemDataUtils.getDataMap(itemStack));
        }
        if (tile instanceof ISustainedData) {
            ((ISustainedData) tile).writeSustainedData(itemStack);
        }
        if (tile.supportsRedstone()) {
            ItemDataUtils.setInt(itemStack, "controlType", tile.getControlType().ordinal());
        }
        if (item instanceof ISustainedInventory && tile.hasInventory() && tile.getSlots() > 0) {
            ((ISustainedInventory) item).setInventory(((ISustainedInventory) tile).getInventory(), itemStack);
        }
        if (item instanceof ISustainedTank && tile instanceof ISustainedTank) {
            FluidStack fluidStack = ((ISustainedTank) tile).getFluidStack();
            if (!fluidStack.isEmpty()) {
                ISustainedTank sustainedTank = (ISustainedTank) item;
                if (sustainedTank.hasTank(itemStack)) {
                    sustainedTank.setFluidStack(fluidStack, itemStack);
                }
            }
        }
        if (item instanceof IEnergizedItem && tile.isElectric() && !(tile instanceof TileEntityMultiblock<?>)) {
            ((IEnergizedItem) item).setEnergy(itemStack, tile.getEnergy());
        }
        return itemStack;
    }

    protected ItemStack setItemData(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull TileEntityMekanism tile, @Nonnull ItemStack stack) {
        return stack;
    }

    /**
     * {@inheritDoc} Used together with {@link Block#removedByPlayer(BlockState, World, BlockPos, PlayerEntity, boolean, IFluidState)}.
     *
     * @author Forge
     * @see FlowerPotBlock#harvestBlock(World, PlayerEntity, BlockPos, BlockState, TileEntity, ItemStack)
     */
    @Override
    public void harvestBlock(@Nonnull World world, PlayerEntity player, @Nonnull BlockPos pos, @Nonnull BlockState state, TileEntity te, @Nonnull ItemStack stack) {
        //TODO: Replace the below stuff with allowing the drop to spawn from the loot table, or at least for now fall back to the loot table if there is no TE
        player.addStat(Stats.BLOCK_MINED.get(this));
        player.addExhaustion(0.005F);
        if (!world.isRemote) {
            ItemStack dropItem = getDropItem(state, world, pos);
            if (te instanceof INameable) {
                dropItem.setDisplayName(((INameable) te).getName());
            }
            spawnAsEntity(world, pos, dropItem);
        }
        //Set it to air like the flower pot's harvestBlock method
        world.removeBlock(pos, false);
    }

    /**
     * {@inheritDoc} Keep tile entity in world until after {@link Block#getDrops}. Used together with {@link Block#harvestBlock(World, PlayerEntity, BlockPos, BlockState,
     * TileEntity, ItemStack)}.
     *
     * @author Forge
     * @see FlowerPotBlock#removedByPlayer(BlockState, World, BlockPos, PlayerEntity, boolean, IFluidState)
     */
    @Override
    public boolean removedByPlayer(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, boolean willHarvest, IFluidState fluidState) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false, fluidState);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlayerEntity player) {
        return getDropItem(state, world, pos);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return this instanceof IHasTileEntity<?>;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        if (this instanceof IHasTileEntity<?>) {
            return ((IHasTileEntity<?>) this).getTileType().create();
        }
        return null;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        BlockStateHelper.fillBlockStateContainer(this, builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return BlockStateHelper.getStateForPlacement(this, super.getStateForPlacement(context), context);
    }

    @Nonnull
    @Override
    @Deprecated
    public IFluidState getFluidState(BlockState state) {
        if (state.getBlock() instanceof IStateWaterLogged) {
            return ((IStateWaterLogged) state.getBlock()).getFluid(state);
        }
        return super.getFluidState(state);
    }

    @Nonnull
    @Override
    public BlockState updatePostPlacement(BlockState state, Direction facing, @Nonnull BlockState facingState, @Nonnull IWorld world, @Nonnull BlockPos currentPos,
          @Nonnull BlockPos facingPos) {
        if (state.getBlock() instanceof IStateWaterLogged) {
            ((IStateWaterLogged) state.getBlock()).updateFluids(state, world, currentPos);
        }
        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return;
        }
        //TODO: Remove most implementations of ItemBlock#placeBlockAt and use this method instead
        //TODO: Should this just be TileEntity and then check instance of and abstract things further
        if (tile.supportsRedstone()) {
            tile.redstone = world.isBlockPowered(pos);
        }

        if (tile instanceof IBoundingBlock) {
            ((IBoundingBlock) tile).onPlace();
        }

        /*
        //TODO: Block Basic had this, figure out why
        world.markBlockRangeForRenderUpdate(pos, pos.add(1, 1, 1));
        world.checkLightFor(EnumSkyBlock.BLOCK, pos);
        world.checkLightFor(EnumSkyBlock.SKY, pos);
         */
        if (!world.isRemote) {
            if (tile instanceof IMultiblock) {
                ((IMultiblock<?>) tile).doUpdate();
            }
            if (tile instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) tile).doUpdate();
            }
        }

        //Handle item
        Item item = stack.getItem();
        setTileData(world, pos, state, placer, stack, tile);

        if (item instanceof ISecurityItem && tile.hasSecurity()) {
            ISecurityItem securityItem = (ISecurityItem) item;
            tile.getSecurity().setMode(securityItem.getSecurity(stack));
            UUID ownerUUID = securityItem.getOwnerUUID(stack);
            tile.getSecurity().setOwnerUUID(ownerUUID == null ? placer.getUniqueID() : ownerUUID);
        }
        if (tile.supportsUpgrades() && ItemDataUtils.hasData(stack, "upgrades")) {
            tile.getComponent().read(ItemDataUtils.getDataMap(stack));
        }
        if (tile instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tile;
            if (ItemDataUtils.hasData(stack, "sideDataStored")) {
                config.getConfig().read(ItemDataUtils.getDataMap(stack));
                config.getEjector().read(ItemDataUtils.getDataMap(stack));
            }
        }
        if (tile instanceof ISustainedData && stack.hasTag()) {
            ((ISustainedData) tile).readSustainedData(stack);
        }
        if (tile.supportsRedstone()) {
            if (ItemDataUtils.hasData(stack, "controlType")) {
                tile.setControlType(RedstoneControl.byIndexStatic(ItemDataUtils.getInt(stack, "controlType")));
            }
        }
        if (item instanceof ISustainedTank && tile instanceof ISustainedTank && ((ISustainedTank) item).hasTank(stack)) {
            FluidStack fluid = ((ISustainedTank) item).getFluidStack(stack);
            if (!fluid.isEmpty()) {
                ((ISustainedTank) tile).setFluidStack(fluid);
            }
        }
        if (item instanceof ISustainedInventory && tile.hasInventory()) {
            tile.setInventory(((ISustainedInventory) item).getInventory(stack));
        }
        //The variant of it that was in BlockBasic
        if (item instanceof IItemEnergized && tile.isElectric() && !(tile instanceof TileEntityMultiblock<?>)) {
            tile.setEnergy(((IItemEnergized) item).getEnergy(stack));
        }
        //TODO: Figure out if this is actually needed
        if (!world.isRemote) {
            Mekanism.packetHandler.sendUpdatePacket(tile);
        }
    }

    //TODO: Method to override for setting some simple tile specific stuff
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, TileEntityMekanism tile) {

    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rotation) {
        return BlockStateHelper.rotate(state, world, pos, rotation);
    }

    @Nonnull
    @Override
    public BlockState rotate(@Nonnull BlockState state, Rotation rotation) {
        return BlockStateHelper.rotate(state, rotation);
    }

    @Nonnull
    @Override
    public BlockState mirror(@Nonnull BlockState state, Mirror mirror) {
        return BlockStateHelper.mirror(state, mirror);
    }

    @Override
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof IBoundingBlock) {
                ((IBoundingBlock) tile).onBreak();
            }
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState blockState) {
        return this instanceof ISupportsComparator;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        if (hasComparatorInputOverride(blockState)) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            //Double check the tile actually has comparator support
            //TODO: Eventually make it so this is not needed
            if (tile instanceof IComparatorSupport) {
                return ((IComparatorSupport) tile).getRedstoneLevel();
            }
        }
        return 0;
    }
}