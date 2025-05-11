package mekanism.common.item.predicate;

import mekanism.common.Mekanism;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate.Type;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismItemPredicates {

    public static final DeferredRegister<Type<?>> PREDICATES = DeferredRegister.create(Registries.ITEM_SUB_PREDICATE_TYPE, Mekanism.MODID);

    public static final DeferredHolder<Type<?>, Type<FullCanteenItemPredicate>> FULL_CANTEEN = PREDICATES.register("full_canteen", () -> FullCanteenItemPredicate.TYPE);
    public static final DeferredHolder<ItemSubPredicate.Type<?>, ItemSubPredicate.Type<MaxedModuleContainerItemPredicate>> MAXED_MODULE_CONTAINER_ITEM = PREDICATES.register("maxed_module_container", () -> MaxedModuleContainerItemPredicate.TYPE);
}
