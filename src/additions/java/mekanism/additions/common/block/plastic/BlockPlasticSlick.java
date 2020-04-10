package mekanism.additions.common.block.plastic;

import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import net.minecraft.block.Block;

public class BlockPlasticSlick extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticSlick(EnumColor color) {
        super(Block.Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F).slipperiness(0.98F));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}