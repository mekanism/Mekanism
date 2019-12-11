package mekanism.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

//TODO: Ask the main block about the voxel shape this bounding block should have
//TODO: Extend BlockTileDrops instead and rename it to MekanismBlock. Not done yet as checking is needed to ensure how drops happen
// still happens correctly and things in the super class don't mess this up
public class BlockBounding extends Block implements IHasTileEntity<TileEntityBoundingBlock> {

    @Nullable
    private static BlockPos getMainBlockPos(IBlockReader world, BlockPos thisPos) {
        TileEntityBoundingBlock te = MekanismUtils.getTileEntity(TileEntityBoundingBlock.class, world, thisPos);
        if (te != null && te.receivedCoords && !thisPos.equals(te.getMainPos())) {
            return te.getMainPos();
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
            if (!state.isAir(world, mainPos)) {
                //Set the main block to air, which will invalidate the rest of the bounding blocks
                world.removeBlock(mainPos, false);
            }
        }
    }

    private final boolean advanced;

    public BlockBounding(boolean advanced) {
        //TODO: Should we have two blocks, one for advanced one for not advanced like it is now, or should it be a blockstate
        // It probably should be a blockstate
        //Note: We require setting variable opacity so that the block state does not cache the ability of if blocks can be placed on top of the bounding block
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F).variableOpacity());
        this.advanced = advanced;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return false;
        }
        BlockState state1 = world.getBlockState(mainPos);
        //TODO: Use proper ray trace result, currently is using the one we got but we probably should make one with correct position information
        return state1.getBlock().onBlockActivated(state1, world, mainPos, player, hand, hit);
    }

    @Override
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        //Remove the main block if a bounding block gets broken by being directly replaced
        removeMainBlock(world, pos);
        super.onReplaced(state, world, pos, newState, isMoving);
        world.removeTileEntity(pos);
    }

    /**
     * {@inheritDoc} Delegate to main {@link Block#getPickBlock(BlockState, RayTraceResult, IBlockReader, BlockPos, PlayerEntity)}.
     */
    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlayerEntity player) {
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
     * @see FlowerPotBlock#removedByPlayer(BlockState, World, BlockPos, PlayerEntity, boolean, IFluidState)
     */
    @Override
    public boolean removedByPlayer(@Nonnull BlockState state, World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, boolean willHarvest, IFluidState fluidState) {
        if (willHarvest) {
            return true;
        }
        removeMainBlock(world, pos);
        return super.removedByPlayer(state, world, pos, player, false, fluidState);
    }

    /**
     * {@inheritDoc} Delegate to main {@link Block#getDrops(NonNullList, IBlockReader, BlockPos, BlockState, int)}.
     */
    //TODO: Loot table? Or how should the bounding block handle drops
    /*@Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, IBlockReader world, BlockPos pos, @Nonnull BlockState state, int fortune) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return;
        }
        BlockState state1 = world.getBlockState(mainPos);
        state1.getBlock().getDrops(drops, world, mainPos, state1, fortune);
    }*/

    /**
     * {@inheritDoc} Used together with {@link Block#removedByPlayer(BlockState, World, BlockPos, PlayerEntity, boolean, IFluidState)}.
     *
     * @author Forge
     * @see FlowerPotBlock#harvestBlock(World, PlayerEntity, BlockPos, BlockState, TileEntity, ItemStack)
     */
    @Override
    public void harvestBlock(@Nonnull World world, PlayerEntity player, @Nonnull BlockPos pos, @Nonnull BlockState state, TileEntity te, @Nonnull ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.removeBlock(pos, false);
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        TileEntityBoundingBlock tile = MekanismUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
        if (tile != null) {
            tile.onNeighborChange(state.getBlock());
        }
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos != null) {
            world.getBlockState(mainPos).neighborChanged(world, mainPos, neighborBlock, neighborPos, isMoving);
        }
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return super.getPlayerRelativeBlockHardness(state, player, world, pos);
        }
        return world.getBlockState(mainPos).getPlayerRelativeBlockHardness(player, world, mainPos);
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
        return getTileType().create();
    }

    @Override
    public TileEntityType<TileEntityBoundingBlock> getTileType() {
        if (advanced) {
            return MekanismTileEntityTypes.ADVANCED_BOUNDING_BLOCK.getTileEntityType();
        }
        return MekanismTileEntityTypes.BOUNDING_BLOCK.getTileEntityType();
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        //TODO: Check if this is the ideal solution or do we want to offload it to the main block somehow
        return BlockRenderLayer.CUTOUT;
    }

    //TODO: If need be override: getCollisionShape, getRenderShape, getRaytraceShape to allow for
    // the blocks to properly proxy that stuff, if they have overridden implementations of the methods
    // rather than have them basically have the defaults which is just based off of getShape
    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        //TODO: Fix not being able to place a torch on the back of the miner
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            //If we don't have a main pos, then act as if the block is empty so that we can move into it properly
            return VoxelShapes.empty();
        }
        BlockState mainState = world.getBlockState(mainPos);
        VoxelShape shape = mainState.getShape(world, mainPos, context);
        BlockPos offset = pos.subtract(mainPos);
        //TODO: Can we somehow cache the withOffset? It potentially would have to then be moved into the Tile, but that is probably fine
        return shape.withOffset(-offset.getX(), -offset.getY(), -offset.getZ());
    }
}