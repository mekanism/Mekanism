package mekanism.common.block;

import java.util.Collections;
import java.util.List;
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

    public BlockOre(INamedResource resource) {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3F, 5F));
        this.resource = resource;
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.resource.getRegistrySuffix() + "_ore"));
    }

    @Override
    public List<String> getOredictEntries() {
        return Collections.singletonList("ore" + resource.getOreSuffix());
    }
}