package mekanism.common.registration.impl;

import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class ItemRegistryObject<ITEM extends Item> extends WrappedRegistryObject<Item, ITEM> implements IItemProvider {

    public ItemRegistryObject(DeferredHolder<Item, ITEM> registryObject) {
        super(registryObject);
    }

    @NotNull
    @Override
    public ITEM asItem() {
        return get();
    }
}