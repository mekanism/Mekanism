package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.SoundEventDeferredRegister;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.util.SoundEvent;

public final class MekanismSounds {

    public static final SoundEventDeferredRegister SOUND_EVENTS = new SoundEventDeferredRegister(Mekanism.MODID);

    public static final SoundEventRegistryObject<SoundEvent> CHARGEPAD = SOUND_EVENTS.register("tile.machine.chargepad");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_CRYSTALLIZER = SOUND_EVENTS.register("tile.machine.chemical_crystallizer");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_DISSOLUTION_CHAMBER = SOUND_EVENTS.register("tile.machine.chemical_dissolution_chamber");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_INFUSER = SOUND_EVENTS.register("tile.machine.chemical_infuser");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_INJECTION_CHAMBER = SOUND_EVENTS.register("tile.machine.chemical_injection_chamber");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_OXIDIZER = SOUND_EVENTS.register("tile.machine.chemical_oxidizer");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_WASHER = SOUND_EVENTS.register("tile.machine.chemical_washer");
    public static final SoundEventRegistryObject<SoundEvent> COMBINER = SOUND_EVENTS.register("tile.machine.combiner");
    public static final SoundEventRegistryObject<SoundEvent> COMPRESSOR = SOUND_EVENTS.register("tile.machine.compressor");
    public static final SoundEventRegistryObject<SoundEvent> CRUSHER = SOUND_EVENTS.register("tile.machine.crusher");
    public static final SoundEventRegistryObject<SoundEvent> ELECTROLYTIC_SEPARATOR = SOUND_EVENTS.register("tile.machine.electrolytic_separator");
    public static final SoundEventRegistryObject<SoundEvent> ENRICHMENT_CHAMBER = SOUND_EVENTS.register("tile.machine.enrichment_chamber");
    public static final SoundEventRegistryObject<SoundEvent> LASER = SOUND_EVENTS.register("tile.machine.laser");
    public static final SoundEventRegistryObject<SoundEvent> LOGISTICAL_SORTER = SOUND_EVENTS.register("tile.machine.logistical_sorter");
    public static final SoundEventRegistryObject<SoundEvent> METALLURGIC_INFUSER = SOUND_EVENTS.register("tile.machine.metallurgic_infuser");
    public static final SoundEventRegistryObject<SoundEvent> PRECISION_SAWMILL = SOUND_EVENTS.register("tile.machine.precision_sawmill");
    public static final SoundEventRegistryObject<SoundEvent> PRESSURIZED_REACTION_CHAMBER = SOUND_EVENTS.register("tile.machine.pressurized_reaction_chamber");
    public static final SoundEventRegistryObject<SoundEvent> PURIFICATION_CHAMBER = SOUND_EVENTS.register("tile.machine.purification_chamber");
    public static final SoundEventRegistryObject<SoundEvent> RESISTIVE_HEATER = SOUND_EVENTS.register("tile.machine.resistive_heater");
    public static final SoundEventRegistryObject<SoundEvent> ROTARY_CONDENSENTRATOR = SOUND_EVENTS.register("tile.machine.rotary_condensentrator");
    public static final SoundEventRegistryObject<SoundEvent> ENERGIZED_SMELTER = SOUND_EVENTS.register("tile.machine.energized_smelter");

    public static final SoundEventRegistryObject<SoundEvent> FLAMETHROWER_IDLE = SOUND_EVENTS.register("item.flamethrower.idle");
    public static final SoundEventRegistryObject<SoundEvent> FLAMETHROWER_ACTIVE = SOUND_EVENTS.register("item.flamethrower.active");
    public static final SoundEventRegistryObject<SoundEvent> GAS_MASK = SOUND_EVENTS.register("item.gas_mask");
    public static final SoundEventRegistryObject<SoundEvent> JETPACK = SOUND_EVENTS.register("item.jetpack");
    public static final SoundEventRegistryObject<SoundEvent> HYDRAULIC = SOUND_EVENTS.register("item.hydraulic");
    public static final SoundEventRegistryObject<SoundEvent> CJ_EASTER_EGG = SOUND_EVENTS.register("etc.cj");
    //TODO: Should these be removed, they are unused
    public static final SoundEventRegistryObject<SoundEvent> CHRISTMAS1 = SOUND_EVENTS.register("tile.christmas.1");
    public static final SoundEventRegistryObject<SoundEvent> CHRISTMAS2 = SOUND_EVENTS.register("tile.christmas.2");
    public static final SoundEventRegistryObject<SoundEvent> CHRISTMAS3 = SOUND_EVENTS.register("tile.christmas.3");
    public static final SoundEventRegistryObject<SoundEvent> CHRISTMAS4 = SOUND_EVENTS.register("tile.christmas.4");
    public static final SoundEventRegistryObject<SoundEvent> CHRISTMAS5 = SOUND_EVENTS.register("tile.christmas.5");
}