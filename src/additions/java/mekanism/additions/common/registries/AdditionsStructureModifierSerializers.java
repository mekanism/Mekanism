package mekanism.additions.common.registries;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.world.modifier.BabyEntitySpawnStructureModifier;
import mekanism.common.registration.DatapackDeferredRegister;
import mekanism.common.registration.DeferredMapCodecHolder;
import net.neoforged.neoforge.common.world.StructureModifier;

public class AdditionsStructureModifierSerializers {

    public static final DatapackDeferredRegister<StructureModifier> STRUCTURE_MODIFIER_SERIALIZERS = DatapackDeferredRegister.structureModifiers(MekanismAdditions.MODID);

    public static final DeferredMapCodecHolder<StructureModifier, BabyEntitySpawnStructureModifier> SPAWN_BABIES = STRUCTURE_MODIFIER_SERIALIZERS.registerCodec("baby_mob_spawn", BabyEntitySpawnStructureModifier::makeCodec);
}