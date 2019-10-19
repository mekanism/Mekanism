package mekanism.additions.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.EntityObsidianTNT;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

//TODO: Extend TNTBlock?
public class BlockObsidianTNT extends Block {

    public BlockObsidianTNT() {
        super(Block.Properties.create(Material.TNT));
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, "obsidian_tnt"));
    }

    @Override
    @Deprecated
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, world, pos, oldState, isMoving);
        if (world.isBlockPowered(pos)) {
            explode(world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (world.isBlockPowered(pos)) {
            explode(world, pos);
            world.removeBlock(pos, isMoving);
        }
    }

    @Override
    public void onExplosionDestroy(World world, @Nonnull BlockPos pos, @Nonnull Explosion explosion) {
        if (!world.isRemote) {
            EntityObsidianTNT entity = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
            entity.fuse = world.rand.nextInt(entity.fuse / 4) + entity.fuse / 8;
            world.addEntity(entity);
        }
    }

    public void explode(World world, BlockPos pos) {
        explode(world, pos, null);
    }

    private void explode(World world, BlockPos pos, @Nullable LivingEntity exploder) {
        if (!world.isRemote) {
            EntityObsidianTNT entity = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
            world.addEntity(entity);
            entity.playSound(SoundEvents.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
        }
    }

    @Override
    @Deprecated
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE) {
            explode(world, pos, player);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
            if (item == Items.FLINT_AND_STEEL) {
                itemstack.damageItem(1, player, entity -> entity.sendBreakAnimation(hand));
            } else {
                itemstack.shrink(1);
            }
            return true;
        }
        return super.onBlockActivated(state, world, pos, player, hand, hit);
    }

    @Override
    @Deprecated
    public boolean canDropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public void onProjectileCollision(World world, BlockState state, BlockRayTraceResult hit, Entity projectile) {
        if (!world.isRemote && projectile instanceof AbstractArrowEntity) {
            AbstractArrowEntity arrow = (AbstractArrowEntity) projectile;
            Entity entity = arrow.getShooter();
            if (arrow.isBurning()) {
                BlockPos blockpos = hit.getPos();
                explode(world, blockpos, entity instanceof LivingEntity ? (LivingEntity) entity : null);
                world.removeBlock(blockpos, false);
            }
        }
    }
}