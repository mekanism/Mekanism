package mekanism.additions.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.additions.common.entity.EntityObsidianTNT;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TNTBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockObsidianTNT extends TNTBlock {

    public BlockObsidianTNT() {
        super(Block.Properties.create(Material.TNT));
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        //300 is 100% chance fire will spread to this block, 100 is default for TNT
        // Given we are "obsidian" make ours slightly more stable against fire being spread than vanilla TNT
        return 75;
    }

    @Override
    public void catchFire(BlockState state, World world, @Nonnull BlockPos pos, @Nullable Direction side, @Nullable LivingEntity igniter) {
        if (!world.isRemote) {
            TNTEntity tnt = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, igniter);
            world.addEntity(tnt);
            world.playSound(null, tnt.posX, tnt.posY, tnt.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public void onExplosionDestroy(World world, @Nonnull BlockPos pos, @Nonnull Explosion explosion) {
        if (!world.isRemote) {
            TNTEntity tnt = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, explosion.getExplosivePlacedBy());
            tnt.setFuse((short) (world.rand.nextInt(tnt.getFuse() / 4) + tnt.getFuse() / 8));
            world.addEntity(tnt);
        }
    }
}