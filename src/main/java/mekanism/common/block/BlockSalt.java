package mekanism.common.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItem;
import mekanism.common.block.interfaces.IBlockOreDict;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class BlockSalt extends Block implements IBlockOreDict {

    public BlockSalt() {
        super(Material.SAND);
        setHardness(0.5F);
        setSoundType(SoundType.SAND);
        setRegistryName(new ResourceLocation(Mekanism.MODID, "salt_block"));
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

    @Override
    public List<String> getOredictEntries() {
        return Collections.singletonList("blockSalt");
    }
}