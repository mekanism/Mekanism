package mekanism.common.block;

import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockSalt extends Block {

    public BlockSalt() {
        super(Block.Properties.create(Material.SAND).hardnessAndResistance(0.5F, 0).sound(SoundType.SAND));
        setRegistryName(new ResourceLocation(Mekanism.MODID, "block_salt"));
    }

    //TODO: Put drops in loot table
    /*@Nonnull
    @Override
    public Item getItemDropped(BlockState state, Random random, int fortune) {
        return MekanismItem.SALT.getItem();
    }

    @Override
    public int quantityDropped(Random random) {
        return 4;
    }*/
}