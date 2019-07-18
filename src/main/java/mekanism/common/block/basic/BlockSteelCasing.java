package mekanism.common.block.basic;

import mekanism.common.block.BlockBasic;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockSteelCasing extends BlockBasic {

    public BlockSteelCasing() {
        super("steel_casing");
    }

    @Override
    public boolean hasDescription() {
        return true;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return 9F;
    }
}