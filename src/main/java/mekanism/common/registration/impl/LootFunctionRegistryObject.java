package mekanism.common.registration.impl;

import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class LootFunctionRegistryObject<TYPE extends LootItemFunctionType> extends MekanismDeferredHolder<LootItemFunctionType, TYPE> {

    public LootFunctionRegistryObject(ResourceKey<LootItemFunctionType> key) {
        super(key);
    }
}