package mekanism.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.block.states.BlockStateBounding;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBounding extends Block implements IBlockMekanism {

    @Nullable
    private static BlockPos getMainBlockPos(IBlockAccess world, BlockPos thisPos) {
        TileEntity te = world.getTileEntity(thisPos);
        if (te instanceof TileEntityBoundingBlock && !thisPos.equals(((TileEntityBoundingBlock) te).getMainPos())) {
            return ((TileEntityBoundingBlock) te).getMainPos();
        }
        return null;
    }

    /**
     * Removes the main block if it is not already air.
     */
    private static void removeMainBlock(World world, BlockPos thisPos) {
        BlockPos mainPos = getMainBlockPos(world, thisPos);
        if (mainPos != null) {
            IBlockState state = world.getBlockState(mainPos);
            if (!state.getBlock().isAir(state, world, mainPos)) {
                //Set the main block to air, which will invalidate the rest of the bounding blocks
                world.setBlockToAir(mainPos);
            }
        }
    }

    public BlockBounding() {
        super(Material.IRON);
        setHardness(3.5F);
        setResistance(8F);
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateBounding(this);
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockStateBounding.advancedProperty, meta > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockStateBounding.advancedProperty) ? 1 : 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return false;
        }
        IBlockState state1 = world.getBlockState(mainPos);
        return state1.getBlock().onBlockActivated(world, mainPos, state1, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        //Remove the main block if a bounding block gets broken by being directly replaced
        removeMainBlock(world, pos);
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    /**
     * {@inheritDoc} Delegate to main {@link Block#getPickBlock(IBlockState, RayTraceResult, World, BlockPos, EntityPlayer)}.
     */
    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ItemStack.EMPTY;
        }
        IBlockState state1 = world.getBlockState(mainPos);
        return state1.getBlock().getPickBlock(state1, target, world, mainPos, player);
    }

    /**
     * {@inheritDoc} Keep tile entity in world until after {@link Block#getDrops(NonNullList, IBlockAccess, BlockPos, IBlockState, int)}. Used together with {@link
     * Block#harvestBlock(World, EntityPlayer, BlockPos, IBlockState, TileEntity, ItemStack)}.
     *
     * @author Forge
     * @see BlockFlowerPot#removedByPlayer(IBlockState, World, BlockPos, EntityPlayer, boolean)
     */
    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
        if (willHarvest) {
            return true;
        }
        removeMainBlock(world, pos);
        return super.removedByPlayer(state, world, pos, player, false);
    }

    /**
     * {@inheritDoc} Delegate to main {@link Block#getDrops(NonNullList, IBlockAccess, BlockPos, IBlockState, int)}.
     */
    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return;
        }
        IBlockState state1 = world.getBlockState(mainPos);
        state1.getBlock().getDrops(drops, world, mainPos, state1, fortune);
    }

    /**
     * {@inheritDoc} Used together with {@link Block#removedByPlayer(IBlockState, World, BlockPos, EntityPlayer, boolean)}.
     *
     * @author Forge
     * @see BlockFlowerPot#harvestBlock(World, EntityPlayer, BlockPos, IBlockState, TileEntity, ItemStack)
     */
    @Override
    public void harvestBlock(@Nonnull World world, EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.setBlockToAir(pos);
    }

    /**
     * Returns that this "cannot" be silk touched. This is so that {@link Block#getSilkTouchDrop(IBlockState)} is not called, because only {@link
     * Block#getDrops(NonNullList, IBlockAccess, BlockPos, IBlockState, int)} supports tile entities. Our blocks keep their inventory and other behave like they are being
     * silk touched by default anyway.
     *
     * @return false
     */
    @Override
    @Deprecated
    protected boolean canSilkHarvest() {
        return false;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock) world.getTileEntity(pos);
        if (tileEntity != null) {
            tileEntity.onNeighborChange(state.getBlock());
        }
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            IBlockState state1 = world.getBlockState(mainPos);
            state1.getBlock().neighborChanged(state1, world, mainPos, neighborBlock, neighborPos);
        }
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(IBlockState state, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return super.getPlayerRelativeBlockHardness(state, player, world, pos);
        }
        IBlockState state1 = world.getBlockState(mainPos);
        return state1.getBlock().getPlayerRelativeBlockHardness(state1, player, world, mainPos);
    }

    @Nonnull
    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            TileEntity tile = world.getTileEntity(mainPos);
            if (tile instanceof IBoundingBlock) {
                return ((IBoundingBlock) tile).getOffsetBlockFaceShape(face, pos.subtract(mainPos));
            }
        }
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        if (state.getValue(BlockStateBounding.advancedProperty)) {
            return new TileEntityAdvancedBoundingBlock();
        }
        return new TileEntityBoundingBlock();
    }
}