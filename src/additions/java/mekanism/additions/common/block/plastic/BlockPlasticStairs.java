package mekanism.additions.common.block.plastic;

import mekanism.additions.common.MekanismAdditions;
import mekanism.api.block.IColoredBlock;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticStairs extends StairsBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticStairs(IBlockProvider blockProvider, EnumColor color) {
        super(blockProvider.getBlock().getDefaultState(), Properties.create(BlockPlastic.PLASTIC, color.getMapColor()).hardnessAndResistance(5F, 10F));
        this.color = color;
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, color.registry_prefix + "_plastic_stairs"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}