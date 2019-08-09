package mekanism.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateBounding;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockBounding extends Block implements IHasTileEntity<TileEntityBoundingBlock> {

    @Nullable
    private static BlockPos getMainBlockPos(IBlockReader world, BlockPos thisPos) {
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
            BlockState state = world.getBlockState(mainPos);
            if (!state.getBlock().isAir(state, world, mainPos)) {
                //Set the main block to air, which will invalidate the rest of the bounding blocks
                world.removeBlock(mainPos, false);
            }
        }
    }

    private final String name;

    public BlockBounding() {
        //TODO: Replace meta with two blocks one normal and one advanced with a boolean param
        // Or maybe use blockstate
        super(Material.IRON);
        setHardness(3.5F);
        setResistance(8F);
        this.name = "bounding_block";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateBounding(this);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().with(BlockStateBounding.advancedProperty, meta > 0);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.get(BlockStateBounding.advancedProperty) ? 1 : 0;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return false;
        }
        BlockState state1 = world.getBlockState(mainPos);
        //TODO: Use proper ray trace result
        return state1.getBlock().onBlockActivated(state1, world, mainPos, player, hand, hit1);
    }

    @Override
    public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        //Remove the main block if a bounding block gets broken by being directly replaced
        removeMainBlock(world, pos);
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    /**
     * {@inheritDoc} Delegate to main {@link Block#getPickBlock(BlockState, RayTraceResult, World, BlockPos, PlayerEntity)}.
     */
    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, PlayerEntity player) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ItemStack.EMPTY;
        }
        BlockState state1 = world.getBlockState(mainPos);
        return state1.getBlock().getPickBlock(state1, target, world, mainPos, player);
    }

    /**
     * {@inheritDoc} Keep tile entity in world until after {@link Block#getDrops(NonNullList, IBlockReader, BlockPos, BlockState, int)}. Used together with {@link
     * Block#harvestBlock(World, PlayerEntity, BlockPos, BlockState, TileEntity, ItemStack)}.
     *
     * @author Forge
     * @see FlowerPotBlock#removedByPlayer(BlockState, World, BlockPos, PlayerEntity, boolean)
     */
    @Override
    public boolean removedByPlayer(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, boolean willHarvest) {
        if (willHarvest) {
            return true;
        }
        removeMainBlock(world, pos);
        return super.removedByPlayer(state, world, pos, player, false);
    }

    /**
     * {@inheritDoc} Delegate to main {@link Block#getDrops(NonNullList, IBlockReader, BlockPos, BlockState, int)}.
     */
    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, IBlockReader world, BlockPos pos, @Nonnull BlockState state, int fortune) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return;
        }
        BlockState state1 = world.getBlockState(mainPos);
        state1.getBlock().getDrops(drops, world, mainPos, state1, fortune);
    }

    /**
     * {@inheritDoc} Used together with {@link Block#removedByPlayer(BlockState, World, BlockPos, PlayerEntity, boolean)}.
     *
     * @author Forge
     * @see FlowerPotBlock#harvestBlock(World, PlayerEntity, BlockPos, BlockState, TileEntity, ItemStack)
     */
    @Override
    public void harvestBlock(@Nonnull World world, PlayerEntity player, @Nonnull BlockPos pos, @Nonnull BlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.removeBlock(pos, false);
    }

    /**
     * Returns that this "cannot" be silk touched. This is so that {@link Block#getSilkTouchDrop(BlockState)} is not called, because only {@link
     * Block#getDrops(NonNullList, IBlockReader, BlockPos, BlockState, int)} supports tile entities. Our blocks keep their inventory and other behave like they are being
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
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock) world.getTileEntity(pos);
        if (tileEntity != null) {
            tileEntity.onNeighborChange(state.getBlock());
        }
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            BlockState state1 = world.getBlockState(mainPos);
            state1.getBlock().neighborChanged(state1, world, mainPos, neighborBlock, neighborPos);
        }
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull World world, @Nonnull BlockPos pos) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return super.getPlayerRelativeBlockHardness(state, player, world, pos);
        }
        BlockState state1 = world.getBlockState(mainPos);
        return state1.getBlock().getPlayerRelativeBlockHardness(state1, player, world, mainPos);
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        if (state.get(BlockStateBounding.advancedProperty)) {
            return new TileEntityAdvancedBoundingBlock();
        }
        return new TileEntityBoundingBlock();
    }

    @Nullable
    @Override
    public Class<? extends TileEntityBoundingBlock> getTileClass() {
        //TODO: Advanced?
        return TileEntityBoundingBlock.class;
    }
}