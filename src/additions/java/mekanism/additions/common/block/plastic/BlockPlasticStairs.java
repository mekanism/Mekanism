package mekanism.additions.common.block.plastic;

import mekanism.api.block.IColoredBlock;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import net.minecraft.block.StairsBlock;

public class BlockPlasticStairs extends StairsBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticStairs(IBlockProvider blockProvider, EnumColor color) {
        super(() -> blockProvider.getBlock().getDefaultState(), Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}