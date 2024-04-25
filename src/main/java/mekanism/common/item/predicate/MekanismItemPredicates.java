package mekanism.common.item.predicate;

import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.registries.Registries;

public class MekanismItemPredicates {

    public static final MekanismDeferredRegister<ItemSubPredicate.Type<?>> PREDICATES = new MekanismDeferredRegister<>(Registries.ITEM_SUB_PREDICATE_TYPE, Mekanism.MODID);

    public static final MekanismDeferredHolder<ItemSubPredicate.Type<?>, ItemSubPredicate.Type<FullCanteenItemPredicate>> FULL_CANTEEN = PREDICATES.register("full_canteen", () -> FullCanteenItemPredicate.TYPE);
    public static final MekanismDeferredHolder<ItemSubPredicate.Type<?>, ItemSubPredicate.Type<MaxedModuleContainerItemPredicate>> MAXED_MODULE_CONTAINER_ITEM = PREDICATES.register("maxed_module_container", () -> MaxedModuleContainerItemPredicate.TYPE);
}
