package mekanism.common.block;

import mekanism.common.resource.INamedResource;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/**
 * Block class for handling multiple ore block IDs. 0: Osmium Ore 1: Copper Ore 2: Tin Ore
 *
 * @author AidanBrady
 */
public class BlockOre extends Block {

    private final INamedResource resource;

    public BlockOre(INamedResource resource) {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3F, 5F));
        this.resource = resource;
    }
}