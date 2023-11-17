package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.ItemPredicateDeferredRegister;
import mekanism.common.registration.impl.ItemPredicateRegistryObject;

public class MekanismItemPredicates {

    public static final ItemPredicateDeferredRegister PREDICATES = new ItemPredicateDeferredRegister(Mekanism.MODID);

    public static final ItemPredicateRegistryObject<FullCanteenItemPredicate> FULL_CANTEEN = PREDICATES.register("full_canteen", () -> Codec.unit(FullCanteenItemPredicate.INSTANCE));
    public static final ItemPredicateRegistryObject<MaxedModuleContainerItemPredicate<?>> MAXED_MODULE_CONTAINER_ITEM = PREDICATES.register("maxed_module_container", MaxedModuleContainerItemPredicate::makeCodec);
}
