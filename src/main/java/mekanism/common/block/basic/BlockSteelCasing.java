package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorldReader;

public class BlockSteelCasing extends BlockMekanism implements IHasDescription {

    public BlockSteelCasing() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return 9F;
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_STEEL_CASING;
    }
}