package mekanism.common.block.basic;

import mekanism.common.block.BlockBasic;
import mekanism.common.resource.INamedResource;

public class BlockResource extends BlockBasic {

    private final INamedResource resource;

    public BlockResource(INamedResource resource) {
        super("block_" + resource.getRegistrySuffix());
        this.resource = resource;
    }
}