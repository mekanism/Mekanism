package mekanism.common.block.basic;

import mekanism.common.block.BlockBasic;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockSteelCasing extends BlockBasic implements IBlockDescriptive {

    public BlockSteelCasing() {
        super("steel_casing");
    }

    @Override
    public String getDescription() {
        //TODO: Should name just be gotten from registry name
        return LangUtils.localize("tooltip.mekanism." + this.name);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return 9F;
    }
}