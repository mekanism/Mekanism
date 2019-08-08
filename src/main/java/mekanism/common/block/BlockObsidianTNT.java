package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityObsidianTNT;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

//TODO: Extend BlockTNT?
public class BlockObsidianTNT extends Block {

    public BlockObsidianTNT() {
        super(Material.TNT);
        setRegistryName(new ResourceLocation(Mekanism.MODID, "obsidian_tnt"));
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, BlockState state) {
        super.onBlockAdded(world, pos, state);
        if (world.isBlockPowered(pos)) {
            explode(world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (world.isBlockPowered(pos)) {
            explode(world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public void onExplosionDestroy(World world, @Nonnull BlockPos pos, @Nonnull Explosion explosion) {
        if (!world.isRemote) {
            EntityObsidianTNT entity = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
            entity.fuse = world.rand.nextInt(entity.fuse / 4) + entity.fuse / 8;
            world.spawnEntity(entity);
        }
    }

    public void explode(World world, BlockPos pos) {
        if (!world.isRemote) {
            EntityObsidianTNT entity = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
            world.spawnEntity(entity);
            entity.playSound(SoundEvents.ENTITY_TNT_PRIMED, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity entityplayer, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        ItemStack stack = entityplayer.getHeldItem(hand);
        if (!stack.isEmpty() && (stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE)) {
            explode(world, pos);
            world.removeBlock(pos, false);
            if (stack.getItem() == Items.FLINT_AND_STEEL) {
                stack.damageItem(1, entityplayer);
            } else if (!entityplayer.isCreative()) {
                stack.shrink(1);
            }
            return true;
        }
        return super.onBlockActivated(world, pos, state, entityplayer, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof AbstractArrowEntity && !world.isRemote) {
            AbstractArrowEntity entityarrow = (AbstractArrowEntity) entity;
            if (entityarrow.isBurning()) {
                explode(world, pos);
                world.removeBlock(pos, false);
            }
        }
    }
}