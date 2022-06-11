package mekanism.additions.common.registries;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.world.modifier.BabyEntitySpawnBiomeModifier;
import mekanism.common.registration.impl.BiomeModifierSerializerDeferredRegister;
import mekanism.common.registration.impl.BiomeModifierSerializerRegistryObject;

public class AdditionsBiomeModifierSerializers {

    public static final BiomeModifierSerializerDeferredRegister BIOME_MODIFIER_SERIALIZERS = new BiomeModifierSerializerDeferredRegister(MekanismAdditions.MODID);

    public static final BiomeModifierSerializerRegistryObject<BabyEntitySpawnBiomeModifier> SPAWN_BABIES = BIOME_MODIFIER_SERIALIZERS.register("baby_mob_spawn", BabyEntitySpawnBiomeModifier::makeCodec);
}