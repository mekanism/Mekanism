package mekanism.additions.common.block.plastic;

import mekanism.additions.common.MekanismAdditions;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticFenceGate extends FenceGateBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticFenceGate(EnumColor color) {
        super(Properties.create(Material.CLAY, color.getMapColor()));
        this.color = color;
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, color.registry_prefix + "_plastic_fence_gate"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}