package mekanism.additions.common.registries;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.world.modifier.BabyEntitySpawnBiomeModifier;
import mekanism.common.registration.DatapackDeferredRegister;
import mekanism.common.registration.DeferredCodecHolder;
import net.neoforged.neoforge.common.world.BiomeModifier;

public class AdditionsBiomeModifierSerializers {

    public static final DatapackDeferredRegister<BiomeModifier> BIOME_MODIFIER_SERIALIZERS = DatapackDeferredRegister.biomeModifiers(MekanismAdditions.MODID);

    public static final DeferredCodecHolder<BiomeModifier, BabyEntitySpawnBiomeModifier> SPAWN_BABIES = BIOME_MODIFIER_SERIALIZERS.registerCodec("baby_mob_spawn", BabyEntitySpawnBiomeModifier::makeCodec);
}