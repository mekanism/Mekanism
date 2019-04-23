package mekanism.common.block;

import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockMekanismSimple extends Block {

    protected BlockMekanismSimple(Material materialIn) {
        super(materialIn);
    }

    @Nonnull
    protected abstract ItemStack getDropItem(@Nonnull IBlockState state, @Nonnull IBlockAccess world,
          @Nonnull BlockPos pos);

    /**
     * {@inheritDoc} Used together with {@link Block#removedByPlayer(IBlockState, World, BlockPos, EntityPlayer,
     * boolean)}.
     *
     * @author Forge
     * @see BlockFlowerPot#harvestBlock(World, EntityPlayer, BlockPos, IBlockState, TileEntity, ItemStack)
     */
    @Override
    public void harvestBlock(@Nonnull World world, EntityPlayer player, @Nonnull BlockPos pos,
          @Nonnull IBlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
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