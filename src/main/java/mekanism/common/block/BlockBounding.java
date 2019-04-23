package mekanism.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.states.BlockStateBounding;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BlockBounding extends Block {

    @Nullable
    private static BlockPos getMainBlockPos(World world, BlockPos thisPos) {
        TileEntity te = world.getTileEntity(thisPos);
        if (te instanceof TileEntityBoundingBlock && !thisPos.equals(((TileEntityBoundingBlock) te).mainPos)) {
            return ((TileEntityBoundingBlock) te).mainPos;
        }
        return null;
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
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
          EnumFacing side, float hitX, float hitY, float hitZ) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return false;
        }
        IBlockState state1 = world.getBlockState(mainPos);
        return state1.getBlock().onBlockActivated(world, mainPos, state1, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world,
          @Nonnull BlockPos pos, EntityPlayer player) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return ItemStack.EMPTY;
        }
        IBlockState state1 = world.getBlockState(mainPos);
        return state1.getBlock().getPickBlock(state1, target, world, mainPos, player);
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos,
          @Nonnull EntityPlayer player, boolean willHarvest) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return false;
        }
        IBlockState state1 = world.getBlockState(mainPos);
        return state1.getBlock().removedByPlayer(state1, world, mainPos, player, willHarvest);
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock,
          BlockPos neighborPos) {
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
    public float getPlayerRelativeBlockHardness(IBlockState state, @Nonnull EntityPlayer player, @Nonnull World world,
          @Nonnull BlockPos pos) {
        BlockPos mainPos = getMainBlockPos(world, pos);
        if (mainPos == null) {
            return super.getPlayerRelativeBlockHardness(state, player, world, pos);
        }
        IBlockState state1 = world.getBlockState(mainPos);
        return state1.getBlock().getPlayerRelativeBlockHardness(state1, player, world, mainPos);
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.AIR;
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