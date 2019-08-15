package mekanism.common.block.plastic;

import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticGlow extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticGlow(EnumColor color) {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(5F, 10F).lightValue(10));
        this.color = color;
        setRegistryName(new ResourceLocation(Mekanism.MODID, color.registry_prefix + "_plastic_glow"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}