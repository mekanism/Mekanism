package mekanism.common.block.plastic;

import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlastic extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlastic(EnumColor color) {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(5F, 10F));
        this.color = color;
        setRegistryName(new ResourceLocation(Mekanism.MODID, color.registry_prefix + "_plastic"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}