package mekanism.additions.common.block.plastic;

import mekanism.additions.common.MekanismAdditions;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticGlow extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticGlow(EnumColor color) {
        super(Block.Properties.create(Material.WOOD, color.getMapColor()).hardnessAndResistance(5F, 10F).lightValue(10));
        this.color = color;
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, color.registry_prefix + "_plastic_glow"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}