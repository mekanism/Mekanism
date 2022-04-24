package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistryObject<ITEM extends Item> extends WrappedRegistryObject<ITEM> implements IItemProvider {

    public ItemRegistryObject(RegistryObject<ITEM> registryObject) {
        super(registryObject);
    }

    @Nonnull
    @Override
    public ITEM asItem() {
        return get();
    }
}