package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityObsidianTNT;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockObsidianTNT extends Block {

    public BlockObsidianTNT() {
        super(Material.TNT);
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Override
    public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        super.breakBlock(world, pos, state);

        world.removeTileEntity(pos);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);

        if (world.getRedstonePowerFromNeighbors(pos) > 0) {
            explode(world, pos);
            world.setBlockToAir(pos);
        }
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock,
          BlockPos neighborPos) {
        if (world.getRedstonePowerFromNeighbors(pos) > 0) {
            explode(world, pos);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void onExplosionDestroy(World world, @Nonnull BlockPos pos, @Nonnull Explosion explosion) {
        if (!world.isRemote) {
            EntityObsidianTNT entity = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY() + 0.5F,
                  pos.getZ() + 0.5F);
            entity.fuse = world.rand.nextInt(entity.fuse / 4) + entity.fuse / 8;
            world.spawnEntity(entity);
        }
    }

    public void explode(World world, BlockPos pos) {
        if (!world.isRemote) {
            EntityObsidianTNT entity = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY() + 0.5F,
                  pos.getZ() + 0.5F);
            world.spawnEntity(entity);
            entity.playSound(SoundEvents.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer,
          EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = entityplayer.getHeldItem(hand);

        if (!stack.isEmpty() && stack.getItem() == Items.FLINT_AND_STEEL) {
            explode(world, pos);
            world.setBlockToAir(pos);

            return true;
        } else {
            return super.onBlockActivated(world, pos, state, entityplayer, hand, side, hitX, hitY, hitZ);
        }
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityArrow && !world.isRemote) {
            EntityArrow entityarrow = (EntityArrow) entity;

            if (entityarrow.isBurning()) {
                explode(world, pos);
                world.setBlockToAir(pos);
            }
        }
    }
}
