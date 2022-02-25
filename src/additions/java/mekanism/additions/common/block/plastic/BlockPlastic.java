package mekanism.additions.common.block.plastic;

import java.util.function.UnaryOperator;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class BlockPlastic extends Block implements IColoredBlock {

    public static final Material PLASTIC = new Material.Builder(MaterialColor.CLAY).build();

    private final EnumColor color;

    public BlockPlastic(EnumColor color, UnaryOperator<Properties> propertyModifier) {
        super(BlockStateHelper.applyLightLevelAdjustments(propertyModifier.apply(BlockBehaviour.Properties.of(PLASTIC, color.getMapColor()))));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}