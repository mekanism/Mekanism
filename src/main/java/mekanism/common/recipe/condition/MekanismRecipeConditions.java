package mekanism.common.recipe.condition;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.RecipeConditionDeferredRegister;
import mekanism.common.registration.impl.RecipeConditionRegistryObject;

public class MekanismRecipeConditions {
    public static final RecipeConditionDeferredRegister CONDITION_CODECS = new RecipeConditionDeferredRegister(Mekanism.MODID);

    public static final RecipeConditionRegistryObject<ConditionExistsCondition> CONDITION_EXISTS = CONDITION_CODECS.register("condition_exists", ConditionExistsCondition::makeCodec);
    public static final RecipeConditionRegistryObject<ModVersionLoadedCondition> MOD_VERSION_LOADED = CONDITION_CODECS.register("mod_version_loaded", ModVersionLoadedCondition::makeCodec);
}
