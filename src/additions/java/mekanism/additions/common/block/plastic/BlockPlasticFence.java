package mekanism.additions.common.block.plastic;

import mekanism.additions.common.MekanismAdditions;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticFence extends FenceBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticFence(EnumColor color) {
        super(Block.Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F));
        this.color = color;
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, color.registry_prefix + "_plastic_fence"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}