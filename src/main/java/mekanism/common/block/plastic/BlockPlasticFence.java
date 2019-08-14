package mekanism.common.block.plastic;

import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticFence extends FenceBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticFence(EnumColor color) {
        super(Block.Properties.create(Material.CLAY));
        this.color = color;
        setRegistryName(new ResourceLocation(Mekanism.MODID, color.registry_prefix + "_plastic_fence"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}