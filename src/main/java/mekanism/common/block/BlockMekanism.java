package mekanism.common.block;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.sustained.ISustainedInventory;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeComparator;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Special handling for block drops that need TileEntity data
 */
public abstract class BlockMekanism extends Block {

    protected BlockMekanism(Block.Properties properties) {
        super(properties);
        setDefaultState(BlockStateHelper.getDefaultState(stateContainer.getBaseState()));
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlayerEntity player) {
        ItemStack itemStack = new ItemStack(this);
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return itemStack;
        }
        Item item = itemStack.getItem();
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
            ItemDataUtils.setInt(itemStack, NBTConstants.CONTROL_TYPE, tile.getControlType().ordinal());
        }
        for (SubstanceType type : SubstanceType.values()) {
            if (tile.handles(type)) {
                ItemDataUtils.setList(itemStack, type.getContainerTag(), type.getWriteFunction().apply(type.getContainers(tile)));
            }
        }
        if (item instanceof ISustainedInventory && tile.persistInventory() && tile.getSlots() > 0) {
            ((ISustainedInventory) item).setInventory(((ISustainedInventory) tile).getInventory(), itemStack);
        }
        return itemStack;
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
        if (state.getBlock() instanceof IStateFluidLoggable) {
            return ((IStateFluidLoggable) state.getBlock()).getFluid(state);
        }
        return super.getFluidState(state);
    }

    @Nonnull
    @Override
    public BlockState updatePostPlacement(BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull IWorld world, @Nonnull BlockPos currentPos,
          @Nonnull BlockPos facingPos) {
        if (state.getBlock() instanceof IStateFluidLoggable) {
            ((IStateFluidLoggable) state.getBlock()).updateFluids(state, world, currentPos);
        }
        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return;
        }
        if (tile.supportsRedstone()) {
            tile.redstone = world.isBlockPowered(pos);
        }

        if (tile instanceof IBoundingBlock) {
            ((IBoundingBlock) tile).onPlace();
        }
        if (!world.isRemote()) {
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
        if (tile.supportsUpgrades()) {
            //The read method validates that data is stored
            tile.getComponent().read(ItemDataUtils.getDataMap(stack));
        }
        if (tile instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tile;
            //The read methods validate that data is stored
            config.getConfig().read(ItemDataUtils.getDataMap(stack));
            config.getEjector().read(ItemDataUtils.getDataMap(stack));
        }
        for (SubstanceType type : SubstanceType.values()) {
            if (type.canHandle(tile)) {
                type.getReadFunction().accept(type.getContainers(tile), ItemDataUtils.getList(stack, type.getContainerTag()));
            }
        }
        if (tile instanceof ISustainedData && stack.hasTag()) {
            ((ISustainedData) tile).readSustainedData(stack);
        }
        if (tile.supportsRedstone() && ItemDataUtils.hasData(stack, NBTConstants.CONTROL_TYPE, NBT.TAG_INT)) {
            tile.setControlType(RedstoneControl.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.CONTROL_TYPE)));
        }
        if (item instanceof ISustainedInventory && tile.persistInventory()) {
            tile.setInventory(((ISustainedInventory) item).getInventory(stack));
        }
    }

    //Method to override for setting some simple tile specific stuff
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, TileEntityMekanism tile) {
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rotation) {
        return BlockStateHelper.rotate(state, world, pos, rotation);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState rotate(@Nonnull BlockState state, Rotation rotation) {
        return BlockStateHelper.rotate(state, rotation);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState mirror(@Nonnull BlockState state, Mirror mirror) {
        return BlockStateHelper.mirror(state, mirror);
    }

    @Override
    @Deprecated
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.hasTileEntity() && oldState.getBlock() != state.getBlock()) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onAdded();
            }
        }
        super.onBlockAdded(state, world, pos, oldState, isMoving);
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
        return Attribute.has(this, AttributeComparator.class);
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        if (hasComparatorInputOverride(blockState)) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            //Double check the tile actually has comparator support
            if (tile instanceof IComparatorSupport) {
                IComparatorSupport comparatorTile = (IComparatorSupport) tile;
                if (comparatorTile.supportsComparator()) {
                    return comparatorTile.getCurrentRedstoneLevel();
                }
            }
        }
        return 0;
    }
}