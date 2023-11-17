package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class LootFunctionRegistryObject<TYPE extends LootItemFunctionType> extends WrappedRegistryObject<LootItemFunctionType, TYPE> {

    public LootFunctionRegistryObject(DeferredHolder<LootItemFunctionType, TYPE> registryObject) {
        super(registryObject);
    }
}