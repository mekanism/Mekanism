package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.component.predicate.UpgradeTypeComponentPredicate;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;

public class MekanismDataComponentPredicates {

    private MekanismDataComponentPredicates() {
    }

    public static final MekanismDeferredRegister<DataComponentPredicate.Type<?>> DATA_COMPONENT_PREDICATE_TYPES = new MekanismDeferredRegister<>(Registries.DATA_COMPONENT_PREDICATE_TYPE, Mekanism.MODID);

    public static final MekanismDeferredHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<UpgradeTypeComponentPredicate>> UPGRADES = DATA_COMPONENT_PREDICATE_TYPES.register("upgrades",
          () -> new DataComponentPredicate.ConcreteType<>(UpgradeTypeComponentPredicate.CODEC));
}