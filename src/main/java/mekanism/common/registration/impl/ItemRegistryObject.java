package mekanism.common.registration.impl;

import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class ItemRegistryObject<ITEM extends Item> extends MekanismDeferredHolder<Item, ITEM> implements IItemProvider {

    public ItemRegistryObject(ResourceKey<Item> key) {
        super(key);
    }

    @NotNull
    @Override
    public ITEM asItem() {
        return value();
    }
}