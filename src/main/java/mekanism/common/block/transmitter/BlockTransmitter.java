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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class BlockTransmitter extends BlockTileDrops implements IStateConnection {

    protected BlockTransmitter(String name) {
        super(Material.PISTON);
        setHardness(1F);
        setResistance(10F);
        setRegistryName(new ResourceLocation(Mekanism.MODID, name));
    }

    protected static TileEntitySidedPipe getTileEntitySidedPipe(IWorldReader world, BlockPos pos) {
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
    public BlockState getActualState(@Nonnull BlockState state, IWorldReader world, BlockPos pos) {
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
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return false;
        }
        IMekWrench wrenchHandler = Wrenches.getHandler(stack);
        if (wrenchHandler != null) {
            RayTraceResult raytrace = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos);
            if (wrenchHandler.canUseWrench(player, hand, stack, raytrace) && player.isSneaking()) {
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
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos neighbor) {
        TileEntitySidedPipe tile = getTileEntitySidedPipe(world, pos);
        if (tile != null) {
            Direction side = Direction.getFacingFromVector(neighbor.getX() - pos.getX(), neighbor.getY() - pos.getY(), neighbor.getZ() - pos.getZ());
            tile.onNeighborBlockChange(side);
        }
    }

    @Override
    public void onNeighborChange(IWorldReader world, BlockPos pos, BlockPos neighbor) {
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
    @Deprecated
    public boolean isBlockNormalCube(BlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullBlock(BlockState state) {
        return false;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IWorldReader world, BlockState state, BlockPos pos, Direction face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}