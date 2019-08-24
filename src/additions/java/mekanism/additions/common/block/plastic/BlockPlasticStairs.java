package mekanism.additions.common.block.plastic;

import mekanism.additions.common.MekanismAdditions;
import mekanism.api.IBlockProvider;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticStairs extends StairsBlock implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticStairs(IBlockProvider blockProvider, EnumColor color) {
        super(blockProvider.getBlock().getDefaultState(), Properties.create(Material.CLAY, color.getMapColor()));
        this.color = color;
        setRegistryName(new ResourceLocation(MekanismAdditions.MODID, color.registry_prefix + "_plastic_stairs"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}