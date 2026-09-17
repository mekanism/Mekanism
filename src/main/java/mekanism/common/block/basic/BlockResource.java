package mekanism.common.block.basic;

import mekanism.common.block.BlockMekanism;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.world.level.block.state.BlockBehaviour;

//TODO - 26.3: Can we remove this specific subclass?
public class BlockResource extends BlockMekanism {

    private final BlockResourceInfo resource;

    //TODO: Isn't as "generic"? So make it be from one BlockType thing?
    public BlockResource(BlockBehaviour.Properties properties, BlockResourceInfo resource) {
        super(resource.modifyProperties(properties.requiresCorrectToolForDrops()));
        this.resource = resource;
    }

    public BlockResourceInfo getResourceInfo() {
        return resource;
    }
}