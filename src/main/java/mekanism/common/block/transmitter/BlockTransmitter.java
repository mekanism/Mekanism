package mekanism.common.block.transmitter;

import javax.annotation.Nonnull;
import mekanism.api.IMekWrench;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.AdvancedRayTraceResult;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public abstract class BlockTransmitter extends BlockMekanism implements IStateWaterLogged {

    protected BlockTransmitter() {
        super(Block.Properties.create(Material.PISTON).hardnessAndResistance(1F, 10F));
    }

    @Nonnull
    @Override
    public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return ActionResultType.PASS;
        }
        IMekWrench wrenchHandler = Wrenches.getHandler(stack);
        if (wrenchHandler != null) {
            if (wrenchHandler.canUseWrench(stack, player, hit.getPos()) && player.func_225608_bj_()) {
                if (!world.isRemote) {
                    MekanismUtils.dismantleBlock(state, world, pos);
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntitySidedPipe tile = MekanismUtils.getTileEntity(TileEntitySidedPipe.class, world, pos);
        if (tile != null) {
            tile.onAdded();
        }
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        TileEntitySidedPipe tile = MekanismUtils.getTileEntity(TileEntitySidedPipe.class, world, pos);
        if (tile != null) {
            Direction side = Direction.getFacingFromVector(neighborPos.getX() - pos.getX(), neighborPos.getY() - pos.getY(), neighborPos.getZ() - pos.getZ());
            tile.onNeighborBlockChange(side);
        }
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
        TileEntitySidedPipe tile = MekanismUtils.getTileEntity(TileEntitySidedPipe.class, world, pos);
        if (tile != null) {
            Direction side = Direction.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ());
            tile.onNeighborTileChange(side);
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (!context.hasItem(MekanismItems.CONFIGURATOR.getItem())) {
            return getRealShape(state, world, pos);
        }
        //Get the partial selection box if we are holding a configurator
        if (context.getEntity() == null) {
            //If we don't have an entity get the full VoxelShape
            return getRealShape(state, world, pos);
        }
        TileEntitySidedPipe tile = MekanismUtils.getTileEntity(TileEntitySidedPipe.class, world, pos);
        if (tile == null) {
            //If we failed to get the tile, just give the center shape
            return getCenter();
        }
        Pair<Vec3d, Vec3d> vecs = MultipartUtils.getRayTraceVectors(context.getEntity());
        AdvancedRayTraceResult result = MultipartUtils.collisionRayTrace(pos, vecs.getLeft(), vecs.getRight(), tile.getCollisionBoxes());
        if (result != null && result.valid()) {
            //TODO: Should/does this need to be cached
            return VoxelShapes.create(result.bounds);
        }
        //If we failed to figure it out somehow, just fall back to the center. This should never happen
        return getCenter();
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getRenderShape(BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        //TODO: Check if this should be the same as getShape or is this correct
        return getRealShape(state, world, pos);
    }

    //TODO: Do we need to override getRaytraceShape?

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, ISelectionContext context) {
        //Override this so that we ALWAYS have the full collision box, even if a configurator is being held
        return getRealShape(state, world, pos);
    }

    protected abstract VoxelShape getCenter();

    //TODO: Should we make the edge shapes be different based on the connection type? (Push/Pull)
    protected abstract VoxelShape getRealShape(BlockState state, IBlockReader world, BlockPos pos);
}