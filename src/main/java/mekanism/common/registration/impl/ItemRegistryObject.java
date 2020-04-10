package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

public class ItemRegistryObject<ITEM extends Item> extends WrappedRegistryObject<ITEM> implements IItemProvider {

    public ItemRegistryObject(RegistryObject<ITEM> registryObject) {
        super(registryObject);
    }

    @Nonnull
    @Override
    public ITEM getItem() {
        return get();
    }
}