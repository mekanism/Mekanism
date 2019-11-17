package mekanism.additions.common.block.plastic;

import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;

public class BlockPlasticSlab extends SlabBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticSlab(EnumColor color) {
        super(Block.Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}