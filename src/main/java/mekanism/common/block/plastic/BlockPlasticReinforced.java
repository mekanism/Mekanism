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

public class BlockPlasticReinforced extends Block implements IColoredBlock, IBlockOreDict {

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

    @Override
    public List<String> getOredictEntries() {
        List<String> entries = new ArrayList<>();
        entries.add("blockReinforcedPlastic");
        if (color.dyeName != null) {
            //As of the moment none of the colors used have a null dye name but if the other ones get used this is needed
            entries.add("blockReinforcedPlastic" + color.dyeName);
        }
        return entries;
    }
}