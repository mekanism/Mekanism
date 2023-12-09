package mekanism.common.registration.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceKey;

@NothingNullByDefault
public class DeferredCriterionTrigger<INSTANCE extends CriterionTriggerInstance, TRIGGER extends CriterionTrigger<INSTANCE>> extends MekanismDeferredHolder<CriterionTrigger<?>, TRIGGER> {

    public DeferredCriterionTrigger(ResourceKey<CriterionTrigger<?>> key) {
        super(key);
    }

    public Criterion<INSTANCE> createCriterion(INSTANCE triggerInstance) {
        return value().createCriterion(triggerInstance);
    }
}