package mekanism.additions.common.block.plastic;

import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockPlastic extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlastic(BlockBehaviour.Properties properties, EnumColor color) {
        super(BlockStateHelper.applyLightLevelAdjustments(properties.mapColor(color.getMapColor())));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}