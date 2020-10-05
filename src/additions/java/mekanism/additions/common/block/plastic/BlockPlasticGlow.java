package mekanism.additions.common.block.plastic;

import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraftforge.common.ToolType;

public class BlockPlasticGlow extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticGlow(EnumColor color) {
        super(AbstractBlock.Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F).setLightLevel(state -> 10)
              .harvestTool(ToolType.PICKAXE));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}