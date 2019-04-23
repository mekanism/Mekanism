package mekanism.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

public class BlockSalt extends Block {

    public BlockSalt() {
        super(Material.SAND);
        setCreativeTab(Mekanism.tabMekanism);
        setHardness(0.5F);
        setSoundType(SoundType.SAND);
    }

    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return MekanismItems.Salt;
    }

    @Override
    public int quantityDropped(Random random) {
        return 4;
    }
}
