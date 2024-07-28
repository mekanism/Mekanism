package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.SoundEventDeferredRegister;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.sounds.SoundEvent;

public final class MekanismSounds {

    private MekanismSounds() {
    }

    public static final SoundEventDeferredRegister SOUND_EVENTS = new SoundEventDeferredRegister(Mekanism.MODID);

    public static final SoundEventRegistryObject<SoundEvent> CHARGEPAD = SOUND_EVENTS.register("tile.machine.chargepad");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_CRYSTALLIZER = SOUND_EVENTS.register("tile.machine.chemical_crystallizer");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_DISSOLUTION_CHAMBER = SOUND_EVENTS.register("tile.machine.chemical_dissolution_chamber");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_INFUSER = SOUND_EVENTS.register("tile.machine.chemical_infuser");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_INJECTION_CHAMBER = SOUND_EVENTS.register("tile.machine.chemical_injection_chamber");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_OXIDIZER = SOUND_EVENTS.register("tile.machine.chemical_oxidizer");
    public static final SoundEventRegistryObject<SoundEvent> CHEMICAL_WASHER = SOUND_EVENTS.register("tile.machine.chemical_washer");
    public static final SoundEventRegistryObject<SoundEvent> COMBINER = SOUND_EVENTS.register("tile.machine.combiner");
    public static final SoundEventRegistryObject<SoundEvent> OSMIUM_COMPRESSOR = SOUND_EVENTS.register("tile.machine.compressor");
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
    public static final SoundEventRegistryObject<SoundEvent> ISOTOPIC_CENTRIFUGE = SOUND_EVENTS.register("tile.machine.isotopic_centrifuge");
    public static final SoundEventRegistryObject<SoundEvent> NUTRITIONAL_LIQUIFIER = SOUND_EVENTS.register("tile.machine.nutritional_liquifier");
    public static final SoundEventRegistryObject<SoundEvent> INDUSTRIAL_ALARM = SOUND_EVENTS.register("tile.machine.industrial_alarm");
    public static final SoundEventRegistryObject<SoundEvent> ANTIPROTONIC_NUCLEOSYNTHESIZER = SOUND_EVENTS.register("tile.machine.antiprotonic_nucleosynthesizer");
    public static final SoundEventRegistryObject<SoundEvent> PIGMENT_EXTRACTOR = SOUND_EVENTS.register("tile.machine.pigment_extractor");
    public static final SoundEventRegistryObject<SoundEvent> PIGMENT_MIXER = SOUND_EVENTS.register("tile.machine.pigment_mixer");
    public static final SoundEventRegistryObject<SoundEvent> PAINTING_MACHINE = SOUND_EVENTS.register("tile.machine.painting_machine");
    public static final SoundEventRegistryObject<SoundEvent> SPS = SOUND_EVENTS.register("tile.machine.sps");

    public static final SoundEventRegistryObject<SoundEvent> FLAMETHROWER_IDLE = SOUND_EVENTS.register("item.flamethrower.idle");
    public static final SoundEventRegistryObject<SoundEvent> FLAMETHROWER_ACTIVE = SOUND_EVENTS.register("item.flamethrower.active");
    public static final SoundEventRegistryObject<SoundEvent> SCUBA_MASK = SOUND_EVENTS.register("item.scuba_mask");
    public static final SoundEventRegistryObject<SoundEvent> JETPACK = SOUND_EVENTS.register("item.jetpack");
    public static final SoundEventRegistryObject<SoundEvent> HYDRAULIC = SOUND_EVENTS.register("item.hydraulic");
    public static final SoundEventRegistryObject<SoundEvent> GRAVITATIONAL_MODULATION_UNIT = SOUND_EVENTS.register("item.gravitational_modulation_unit");

    public static final SoundEventRegistryObject<SoundEvent> GEIGER_SLOW = SOUND_EVENTS.register("item.geiger_slow");
    public static final SoundEventRegistryObject<SoundEvent> GEIGER_MEDIUM = SOUND_EVENTS.register("item.geiger_medium");
    public static final SoundEventRegistryObject<SoundEvent> GEIGER_ELEVATED = SOUND_EVENTS.register("item.geiger_elevated");
    public static final SoundEventRegistryObject<SoundEvent> GEIGER_FAST = SOUND_EVENTS.register("item.geiger_fast");

    public static final SoundEventRegistryObject<SoundEvent> BEEP_ON = SOUND_EVENTS.register("gui.digital_beep_on");
    public static final SoundEventRegistryObject<SoundEvent> BEEP_OFF = SOUND_EVENTS.register("gui.digital_beep_off");

    public static final SoundEventRegistryObject<SoundEvent> CHRISTMAS1 = SOUND_EVENTS.register("tile.christmas.1");
    public static final SoundEventRegistryObject<SoundEvent> CHRISTMAS2 = SOUND_EVENTS.register("tile.christmas.2");
    public static final SoundEventRegistryObject<SoundEvent> CHRISTMAS3 = SOUND_EVENTS.register("tile.christmas.3");
    public static final SoundEventRegistryObject<SoundEvent> CHRISTMAS4 = SOUND_EVENTS.register("tile.christmas.4");
    public static final SoundEventRegistryObject<SoundEvent> CHRISTMAS5 = SOUND_EVENTS.register("tile.christmas.5");
}