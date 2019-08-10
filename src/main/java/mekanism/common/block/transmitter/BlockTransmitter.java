package mekanism.common.block.transmitter;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IMekWrench;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateConnection;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public abstract class BlockTransmitter extends BlockTileDrops implements IStateConnection {

    protected BlockTransmitter(String name) {
        super(Block.Properties.create(Material.PISTON).hardnessAndResistance(1F, 10F));
        setRegistryName(new ResourceLocation(Mekanism.MODID, name));
    }

    protected static TileEntitySidedPipe getTileEntitySidedPipe(IBlockReader world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        TileEntitySidedPipe sidedPipe = null;
        if (tileEntity instanceof TileEntitySidedPipe) {
            sidedPipe = (TileEntitySidedPipe) tileEntity;
        } else if (Mekanism.hooks.MCMPLoaded) {
            TileEntity childEntity = MultipartMekanism.unwrapTileEntity(world);
            if (childEntity instanceof TileEntitySidedPipe) {
                sidedPipe = (TileEntitySidedPipe) childEntity;
            }
        }
        return sidedPipe;
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return BlockStateHelper.getBlockState(this);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        //TODO
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState getActualState(@Nonnull BlockState state, IBlockReader world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, getTileEntitySidedPipe(world, pos));
    }

    @Override
    @Deprecated
    public void addCollisionBoxToList(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox,
          @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean b) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile != null) {
            List<AxisAlignedBB> boxes = tile.getCollisionBoxes(entityBox.offset(-pos.getX(), -pos.getY(), -pos.getZ()));
            for (AxisAlignedBB box : boxes) {
                collidingBoxes.add(box.offset(pos));
            }
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return false;
        }
        IMekWrench wrenchHandler = Wrenches.getHandler(stack);
        if (wrenchHandler != null) {
            if (wrenchHandler.canUseWrench(player, hand, stack, hit) && player.isSneaking()) {
                if (!world.isRemote) {
                    MekanismUtils.dismantleBlock(this, state, world, pos);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile != null) {
            tile.onAdded();
        }
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile != null) {
            Direction side = Direction.getFacingFromVector(neighborPos.getX() - pos.getX(), neighborPos.getY() - pos.getY(), neighborPos.getZ() - pos.getZ());
            tile.onNeighborBlockChange(side);
        }
    }

    @Override
    public void onNeighborChange(IBlockReader world, BlockPos pos, BlockPos neighbor) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile != null) {
            Direction side = Direction.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ());
            tile.onNeighborTileChange(side);
        }
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}