package mekanism.common.recipe.condition;

import com.mojang.serialization.Codec;
import mekanism.common.Mekanism;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries.Keys;
import net.neoforged.neoforge.registries.RegistryObject;

public class MekanismRecipeConditions {
    public static final DeferredRegister<Codec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(Keys.CONDITION_CODECS, Mekanism.MODID);

    public static final RegistryObject<Codec<? extends ICondition>> CONDITION_EXISTS = CONDITION_CODECS.register("condition_exists", ConditionExistsCondition::makeCodec);

    public static final RegistryObject<Codec<ModVersionLoadedCondition>> MOD_VERSION_LOADED = CONDITION_CODECS.register("mod_version_loaded", ModVersionLoadedCondition::makeCodec);
}
