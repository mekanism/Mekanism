package mekanism.additions.common.block.plastic;

import mekanism.additions.common.MekanismAdditions;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlastic extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlastic(EnumColor color) {
        super(Block.Properties.create(Material.WOOD, color.getMapColor()).hardnessAndResistance(5F, 10F));
        this.color = color;
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, color.registry_prefix + "_plastic"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}