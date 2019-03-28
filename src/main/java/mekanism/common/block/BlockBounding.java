package mekanism.common.block;

import java.util.Random;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStateBounding;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
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

    public BlockBounding() {
        super(Material.IRON);
        setHardness(3.5F);
        setResistance(8F);
    }

    @Nullable
    private static BlockPos getMainBlockPos(World world, BlockPos thisPos) {
        TileEntity te = world.getTileEntity(thisPos);
        if (te instanceof TileEntityBoundingBlock && !thisPos.equals(((TileEntityBoundingBlock) te).mainPos)) {
            return ((TileEntityBoundingBlock) te).mainPos;
        }
        return null;
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateBounding(this);
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockStateBounding.advancedProperty, meta > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean isAdvanced = state.getValue(BlockStateBounding.advancedProperty);
        return isAdvanced ? 1 : 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
          EnumFacing side, float hitX, float hitY, float hitZ) {
        try {
            BlockPos mainPos = getMainBlockPos(world, pos);
            if (mainPos != null) {
                IBlockState state1 = world.getBlockState(mainPos);
                return state1.getBlock().onBlockActivated(world, mainPos, state1, player, hand, side, hitX, hitY, hitZ);
            }
        } catch (Exception e) {
            Mekanism.logger.error("Something went wrong", e);
        }
        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);

        world.removeTileEntity(pos);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
          EntityPlayer player) {
        try {
            BlockPos mainPos = getMainBlockPos(world, pos);
            if (mainPos != null) {
                IBlockState state1 = world.getBlockState(mainPos);
                return state1.getBlock().getPickBlock(state1, target, world, mainPos, player);
            }
        } catch (Exception e) {
            Mekanism.logger.error("Something went wrong", e);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
          boolean willHarvest) {
        try {
            BlockPos mainPos = getMainBlockPos(world, pos);
            if (mainPos != null) {
                IBlockState state1 = world.getBlockState(mainPos);
                return state1.getBlock().removedByPlayer(state1, world, mainPos, player, willHarvest);
            }
        } catch (Exception e) {
            Mekanism.logger.error("Something went wrong", e);
        }
        return false;
    }

    @Deprecated
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock,
          BlockPos neighborPos) {
        try {
            TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock) world.getTileEntity(pos);
            if (tileEntity != null) {
                tileEntity.onNeighborChange(state.getBlock());
            }
            BlockPos mainPos = getMainBlockPos(world, pos);
            if (mainPos != null) {
                IBlockState state1 = world.getBlockState(mainPos);
                state1.getBlock().neighborChanged(state1, world, mainPos, neighborBlock, neighborPos);
            }
        } catch (Exception e) {
            Mekanism.logger.error("Something went wrong", e);
        }
    }

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
        try {
            BlockPos mainPos = getMainBlockPos(world, pos);
            if (mainPos != null) {
                IBlockState state1 = world.getBlockState(mainPos);
                return state1.getBlock().getPlayerRelativeBlockHardness(state1, player, world, mainPos);
            }
        } catch (Exception e) {
            Mekanism.logger.error("Something went wrong", e);
        }
        return super.getPlayerRelativeBlockHardness(state, player, world, pos);
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return null;
    }

    @Deprecated
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Deprecated
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        if (state.getValue(BlockStateBounding.advancedProperty)) {
            return new TileEntityAdvancedBoundingBlock();
        } else {
            return new TileEntityBoundingBlock();
        }
    }
}
