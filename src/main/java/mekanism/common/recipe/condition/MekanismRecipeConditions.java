package mekanism.common.recipe.condition;

import mekanism.common.Mekanism;
import mekanism.common.registration.DeferredCodecHolder;
import mekanism.common.registration.DeferredCodecRegister;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MekanismRecipeConditions {

    public static final DeferredCodecRegister<ICondition> CONDITION_CODECS = new DeferredCodecRegister<>(NeoForgeRegistries.Keys.CONDITION_CODECS, Mekanism.MODID);

    public static final DeferredCodecHolder<ICondition, ConditionExistsCondition> CONDITION_EXISTS = CONDITION_CODECS.registerCodec("condition_exists", ConditionExistsCondition::makeCodec);
    public static final DeferredCodecHolder<ICondition, ModVersionLoadedCondition> MOD_VERSION_LOADED = CONDITION_CODECS.registerCodec("mod_version_loaded", ModVersionLoadedCondition::makeCodec);
}
