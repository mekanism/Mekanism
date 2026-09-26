package mekanism.common.resource.ore;

import mekanism.common.block.BlockOre;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.world.item.BlockItem;

public record OreBlockType(BlockRegistryObject<BlockOre, BlockItem> stone, BlockRegistryObject<BlockOre, BlockItem> deepslate) {
}