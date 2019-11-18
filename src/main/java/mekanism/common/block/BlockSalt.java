package mekanism.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockSalt extends Block {

    public BlockSalt() {
        super(Block.Properties.create(Material.SAND).hardnessAndResistance(0.5F, 0).sound(SoundType.SAND));
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