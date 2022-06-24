package mekanism.additions.common.registries;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.world.modifier.BabyEntitySpawnStructureModifier;
import mekanism.common.registration.impl.StructureModifierSerializerDeferredRegister;
import mekanism.common.registration.impl.StructureModifierSerializerRegistryObject;

public class AdditionsStructureModifierSerializers {

    public static final StructureModifierSerializerDeferredRegister STRUCTURE_MODIFIER_SERIALIZERS = new StructureModifierSerializerDeferredRegister(MekanismAdditions.MODID);

    public static final StructureModifierSerializerRegistryObject<BabyEntitySpawnStructureModifier> SPAWN_BABIES = STRUCTURE_MODIFIER_SERIALIZERS.register("baby_mob_spawn", BabyEntitySpawnStructureModifier::makeCodec);
}