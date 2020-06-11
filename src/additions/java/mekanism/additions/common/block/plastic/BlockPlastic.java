package mekanism.additions.common.block.plastic;

import mekanism.api.text.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public class BlockPlastic extends Block implements IColoredBlock {

    public static final Material PLASTIC = new Material.Builder(MaterialColor.CLAY).build();

    private final EnumColor color;

    public BlockPlastic(EnumColor color) {
        super(Block.Properties.create(PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}