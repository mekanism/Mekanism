package mekanism.common.block.plastic;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IBlockOreDict;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticGlow extends Block implements IColoredBlock, IBlockOreDict {

    private final EnumColor color;
    private final String name;

    public BlockPlasticGlow(EnumColor color) {
        super(Material.WOOD);
        this.color = color;
        setHardness(5F);
        setResistance(10F);
        //It gets multiplied by 15 when being set
        setLightLevel(10F / 15.0F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = color.registry_prefix + "_plastic_glow";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    public List<String> getOredictEntries() {
        List<String> entries = new ArrayList<>();
        entries.add("blockPlasticGlow");
        if (color.dyeName != null) {
            //As of the moment none of the colors used have a null dye name but if the other ones get used this is needed
            entries.add("blockPlasticGlow" + color.dyeName);
        }
        return entries;
    }
}