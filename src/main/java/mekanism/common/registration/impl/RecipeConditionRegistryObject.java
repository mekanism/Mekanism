package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RecipeConditionRegistryObject<CONDITION extends ICondition> extends WrappedRegistryObject<Codec<? extends ICondition>, Codec<CONDITION>> {

    public RecipeConditionRegistryObject(DeferredHolder<Codec<? extends ICondition>, Codec<CONDITION>> registryObject) {
        super(registryObject);
    }
}