package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemPredicateRegistryObject<PREDICATE extends ICustomItemPredicate> extends WrappedRegistryObject<Codec<? extends ICustomItemPredicate>, Codec<PREDICATE>> {

    public ItemPredicateRegistryObject(DeferredHolder<Codec<? extends ICustomItemPredicate>, Codec<PREDICATE>> registryObject) {
        super(registryObject);
    }
}