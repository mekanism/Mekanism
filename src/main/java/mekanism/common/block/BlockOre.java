package mekanism.common.block;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IBlockOreDict;
import mekanism.common.resource.INamedResource;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

/**
 * Block class for handling multiple ore block IDs. 0: Osmium Ore 1: Copper Ore 2: Tin Ore
 *
 * @author AidanBrady
 */
public class BlockOre extends Block implements IBlockOreDict {

    private final INamedResource resource;
    private final String name;

    public BlockOre(INamedResource resource) {
        super(Material.ROCK);
        this.resource = resource;
        setHardness(3F);
        setResistance(5F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = this.resource.getRegistrySuffix().toLowerCase(Locale.ROOT) + "_ore";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public List<String> getOredictEntries() {
        return Collections.singletonList("ore" + resource.getOreSuffix());
    }
}