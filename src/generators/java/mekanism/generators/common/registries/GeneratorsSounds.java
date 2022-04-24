package mekanism.generators.common.registries;

import mekanism.common.registration.impl.SoundEventDeferredRegister;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.sounds.SoundEvent;

public class GeneratorsSounds {

    private GeneratorsSounds() {
    }

    public static final SoundEventDeferredRegister SOUND_EVENTS = new SoundEventDeferredRegister(MekanismGenerators.MODID);

    public static final SoundEventRegistryObject<SoundEvent> FUSION_REACTOR = SOUND_EVENTS.register("tile.machine.fusion_reactor");
    public static final SoundEventRegistryObject<SoundEvent> BIO_GENERATOR = SOUND_EVENTS.register("tile.generator.bio");
    public static final SoundEventRegistryObject<SoundEvent> GAS_BURNING_GENERATOR = SOUND_EVENTS.register("tile.generator.gas");
    public static final SoundEventRegistryObject<SoundEvent> HEAT_GENERATOR = SOUND_EVENTS.register("tile.generator.heat");
    public static final SoundEventRegistryObject<SoundEvent> SOLAR_GENERATOR = SOUND_EVENTS.register("tile.generator.solar");
    public static final SoundEventRegistryObject<SoundEvent> WIND_GENERATOR = SOUND_EVENTS.register("tile.generator.wind");
    public static final SoundEventRegistryObject<SoundEvent> FISSION_REACTOR = SOUND_EVENTS.register("tile.machine.fission_reactor");
}