package mekanism.additions.common.block.plastic;

import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;

public class BlockPlasticGlow extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticGlow(EnumColor color) {
        super(Block.Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F).lightValue(10));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}