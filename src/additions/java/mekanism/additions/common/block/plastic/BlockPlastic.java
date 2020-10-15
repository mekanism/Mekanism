package mekanism.additions.common.block.plastic;

import java.util.function.UnaryOperator;
import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;

public class BlockPlastic extends Block implements IColoredBlock {

    public static final Material PLASTIC = new Material.Builder(MaterialColor.CLAY).build();

    private final EnumColor color;

    public BlockPlastic(EnumColor color, UnaryOperator<Properties> propertyModifier) {
        super(BlockStateHelper.applyLightLevelAdjustments(propertyModifier.apply(AbstractBlock.Properties.create(PLASTIC, color.getMapColor())
              .harvestTool(ToolType.PICKAXE))));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}