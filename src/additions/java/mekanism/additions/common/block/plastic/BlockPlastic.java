package mekanism.additions.common.block.plastic;

import java.util.function.UnaryOperator;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockPlastic extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlastic(EnumColor color, UnaryOperator<Properties> propertyModifier) {
        super(BlockStateHelper.applyLightLevelAdjustments(propertyModifier.apply(BlockBehaviour.Properties.of().mapColor(color.getMapColor()))));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}