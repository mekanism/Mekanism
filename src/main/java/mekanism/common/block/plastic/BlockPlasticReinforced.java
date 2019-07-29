package mekanism.common.block.plastic;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticReinforced extends Block implements IColoredBlock {

    private final EnumColor color;
    private final String name;

    public BlockPlasticReinforced(EnumColor color) {
        super(Material.WOOD);
        this.color = color;
        setHardness(50F);
        setResistance(2000F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = color.registry_prefix + "_reinforced_plastic";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}