package mekanism.common.registration.impl;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class BlockRegistryObject<BLOCK extends Block, ITEM extends Item> extends DoubleWrappedRegistryObject<Block, BLOCK, Item, ITEM> implements IBlockProvider {

    public BlockRegistryObject(DeferredHolder<Block, BLOCK> blockRegistryObject, DeferredHolder<Item, ITEM> itemRegistryObject) {
        super(blockRegistryObject, itemRegistryObject);
    }

    @NotNull
    @Override
    public BLOCK getBlock() {
        return getPrimary();
    }

    @NotNull
    @Override
    public ITEM asItem() {
        return getSecondary();
    }
}