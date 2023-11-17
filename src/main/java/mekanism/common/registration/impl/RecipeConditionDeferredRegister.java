package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class RecipeConditionDeferredRegister extends WrappedDeferredRegister<Codec<? extends ICondition>> {

    public RecipeConditionDeferredRegister(String modid) {
        super(modid, NeoForgeRegistries.Keys.CONDITION_CODECS);
    }

    public <CONDITION extends ICondition> RecipeConditionRegistryObject<CONDITION> register(String name, Supplier<Codec<CONDITION>> sup) {
        return register(name, sup, RecipeConditionRegistryObject::new);
    }
}