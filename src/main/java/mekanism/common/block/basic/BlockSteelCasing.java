package mekanism.common.block.basic;

import javax.annotation.Nullable;
import mekanism.api.block.IHasModel;
import mekanism.common.block.BlockTileDrops;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorldReader;

public class BlockSteelCasing extends BlockTileDrops implements IHasModel {

    public BlockSteelCasing() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return 9F;
    }
}