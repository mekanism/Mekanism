package mekanism.additions.common.block.plastic;

import mekanism.additions.common.MekanismAdditions;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticSlab extends SlabBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticSlab(EnumColor color) {
        super(Block.Properties.create(Material.CLAY));
        this.color = color;
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, color.registry_prefix + "_plastic_slab"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}