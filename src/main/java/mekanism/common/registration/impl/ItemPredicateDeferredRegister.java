package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ItemPredicateDeferredRegister extends WrappedDeferredRegister<Codec<? extends ICustomItemPredicate>> {

    public ItemPredicateDeferredRegister(String modid) {
        super(modid, NeoForgeRegistries.Keys.ITEM_PREDICATE_SERIALIZERS);
    }

    public <PREDICATE extends ICustomItemPredicate> ItemPredicateRegistryObject<PREDICATE> register(String name, Supplier<Codec<PREDICATE>> sup) {
        return register(name, sup, ItemPredicateRegistryObject::new);
    }
}