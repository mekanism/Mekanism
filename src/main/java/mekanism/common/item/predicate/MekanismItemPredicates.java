package mekanism.common.item.predicate;

import mekanism.common.Mekanism;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismItemPredicates {

    public static final DeferredRegister<DataComponentPredicate.Type<?>> PREDICATES = DeferredRegister.create(Registries.DATA_COMPONENT_PREDICATE_TYPE, Mekanism.MODID);

    public static final DeferredHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<FullCanteenItemPredicate>> FULL_CANTEEN = PREDICATES.register("full_canteen", () -> FullCanteenItemPredicate.TYPE);
    public static final DeferredHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<?>> MAXED_MODULE_CONTAINER_ITEM = PREDICATES.register("maxed_module_container", () -> MaxedModuleContainerItemPredicate.TYPE);
}
