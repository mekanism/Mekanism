package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.BiomeModifierSerializerDeferredRegister;
import mekanism.common.registration.impl.BiomeModifierSerializerRegistryObject;
import mekanism.common.world.modifier.MekanismOreBiomeModifier;
import mekanism.common.world.modifier.MekanismSaltBiomeModifier;

public class MekanismBiomeModifierSerializers {

    public static final BiomeModifierSerializerDeferredRegister BIOME_MODIFIER_SERIALIZERS = new BiomeModifierSerializerDeferredRegister(Mekanism.MODID);

    public static final BiomeModifierSerializerRegistryObject<MekanismOreBiomeModifier> ORE_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("ore", MekanismOreBiomeModifier::makeCodec);
    public static final BiomeModifierSerializerRegistryObject<MekanismSaltBiomeModifier> SALT_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("salt", MekanismSaltBiomeModifier::makeCodec);
}