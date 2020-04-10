package mekanism.additions.common.block.plastic;

import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import net.minecraft.block.Block;

public class BlockPlasticReinforced extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticReinforced(EnumColor color) {
        super(Block.Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(50F, 2000F));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}