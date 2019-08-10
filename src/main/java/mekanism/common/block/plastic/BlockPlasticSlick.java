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

public class BlockPlasticSlick extends Block implements IColoredBlock, IBlockOreDict {

    private final EnumColor color;

    public BlockPlasticSlick(EnumColor color) {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(5F, 10F).slipperiness(0.98F));
        this.color = color;
        setRegistryName(new ResourceLocation(Mekanism.MODID, color.registry_prefix + "_slick_plastic"));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    public List<String> getOredictEntries() {
        List<String> entries = new ArrayList<>();
        entries.add("blockSlickPlastic");
        if (color.dyeName != null) {
            //As of the moment none of the colors used have a null dye name but if the other ones get used this is needed
            entries.add("blockSlickPlastic" + color.dyeName);
        }
        return entries;
    }
}