package mekanism.common.block.plastic;

import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticReinforced extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticReinforced(EnumColor color) {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(50F, 2000F));
        this.color = color;
        setRegistryName(new ResourceLocation(Mekanism.MODID, color.registry_prefix + "_reinforced_plastic"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}