package mekanism.common.block.plastic;

import java.util.Locale;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlastic extends Block implements IColoredBlock {

    private final EnumColor color;
    private final String name;

    public BlockPlastic(EnumColor color) {
        super(Material.WOOD);
        this.color = color;
        setHardness(5F);
        setResistance(10F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = color.dyeName.toLowerCase(Locale.ROOT) + "_plastic";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }
}