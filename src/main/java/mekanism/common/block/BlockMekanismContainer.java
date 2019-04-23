package mekanism.common.block;

import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;

public abstract class BlockMekanismContainer extends BlockContainer {

    protected BlockMekanismContainer(Material materialIn) {
        super(materialIn);
    }

    @Nonnull
    protected abstract ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world,
          @Nonnull BlockPos pos);

    /**
     * {@inheritDoc} Used together with {@link Block#removedByPlayer(IBlockState, World, BlockPos, EntityPlayer,
     * boolean)}.
     * <br>
     * This is like Vanilla's {@link BlockContainer#harvestBlock(World, EntityPlayer, BlockPos, IBlockState, TileEntity,
     * ItemStack)} except that uses the custom {@link ItemStack} from {@link #getDropItem(IBlockState, IBlockAccess,
     * BlockPos)}
     *
     * @author Forge
     * @see BlockFlowerPot#harvestBlock(World, EntityPlayer, BlockPos, IBlockState, TileEntity, ItemStack)
     */
    @Override
    public void harvestBlock(@Nonnull World world, EntityPlayer player, @Nonnull BlockPos pos,
          @Nonnull IBlockState state, TileEntity te, @Nonnull ItemStack stack) {
        StatBase blockStats = StatList.getBlockStats(this);
        if (blockStats != null) {
            player.addStat(blockStats);
        }
        player.addExhaustion(0.005F);
        if (!world.isRemote) {
            Block.spawnAsEntity(world, pos,
                  getDropItem(state, world, pos).setStackDisplayName(((IWorldNameable) te).getName()));
        }
        //Set it to air like the flower pot's harvestBlock method
        world.setBlockToAir(pos);
    }

    /**
     * Returns that this "cannot" be silk touched. This is so that {@link Block#getSilkTouchDrop(IBlockState)} is not
     * called, because only {@link Block#getDrops(NonNullList, IBlockAccess, BlockPos, IBlockState, int)} supports tile
     * entities. Our blocks keep their inventory and other behave like they are being silk touched by default anyway.
     *
     * @return false
     */
    @Override
    @Deprecated
    protected boolean canSilkHarvest() {
        return false;
    }

    @Override
    public void getDrops(@Nonnull NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
          @Nonnull IBlockState state, int fortune) {
        drops.add(getDropItem(state, world, pos));
    }

    /**
     * {@inheritDoc} Keep tile entity in world until after {@link Block#getDrops(NonNullList, IBlockAccess, BlockPos,
     * IBlockState, int)}. Used together with {@link Block#harvestBlock(World, EntityPlayer, BlockPos, IBlockState,
     * TileEntity, ItemStack)}.
     *
     * @author Forge
     * @see BlockFlowerPot#removedByPlayer(IBlockState, World, BlockPos, EntityPlayer, boolean)
     */
    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos,
          @Nonnull EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world,
          @Nonnull BlockPos pos, EntityPlayer player) {
        return getDropItem(state, world, pos);
    }
}