package mekanism.additions.common.block.plastic;

import mekanism.additions.common.MekanismAdditions;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticFenceGate extends FenceGateBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticFenceGate(EnumColor color) {
        super(Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F));
        this.color = color;
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, color.registry_prefix + "_plastic_fence_gate"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}