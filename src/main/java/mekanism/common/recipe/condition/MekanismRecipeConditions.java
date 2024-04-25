package mekanism.common.recipe.condition;

import mekanism.common.Mekanism;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.registration.DeferredMapCodecRegister;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MekanismRecipeConditions {

    public static final DeferredMapCodecRegister<ICondition> CONDITION_CODECS = new DeferredMapCodecRegister<>(NeoForgeRegistries.Keys.CONDITION_CODECS, Mekanism.MODID);

    public static final DeferredMapCodecHolder<ICondition, ConditionExistsCondition> CONDITION_EXISTS = CONDITION_CODECS.registerCodec("condition_exists", ConditionExistsCondition::makeCodec);
    public static final DeferredMapCodecHolder<ICondition, ModVersionLoadedCondition> MOD_VERSION_LOADED = CONDITION_CODECS.registerCodec("mod_version_loaded", ModVersionLoadedCondition::makeCodec);
}
