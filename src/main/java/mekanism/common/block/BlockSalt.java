package mekanism.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItem;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class BlockSalt extends Block {

    private final String name;

    public BlockSalt() {
        super(Material.SAND);
        setCreativeTab(Mekanism.tabMekanism);
        setHardness(0.5F);
        setSoundType(SoundType.SAND);
        this.name = "salt_block";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return MekanismItem.SALT.getItem();
    }

    @Override
    public int quantityDropped(Random random) {
        return 4;
    }
}