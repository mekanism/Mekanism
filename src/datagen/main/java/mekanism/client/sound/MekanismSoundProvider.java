package mekanism.client.sound;

import java.util.function.UnaryOperator;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MekanismSoundProvider extends BaseSoundProvider {

    public MekanismSoundProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper, Mekanism.MODID);
    }

    @Override
    public void registerSounds() {
        addTileSoundEvents();
        addItemSoundEvents();
        addHolidaySoundEvents();
        addGuiSoundEvents();
    }

    private void addTileSoundEvents() {
        String basePath = "tile/";
        addSoundEventWithSubtitle(MekanismSounds.CHARGEPAD, basePath + "chargepad");
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_CRYSTALLIZER, basePath + "chemical_crystallizer");
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_DISSOLUTION_CHAMBER, basePath + "chemical_dissolution_chamber");
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_INFUSER, basePath + "chemical_infuser");
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_INJECTION_CHAMBER, basePath + "chemical_injection_chamber");
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_OXIDIZER, basePath + "chemical_oxidizer");
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_WASHER, basePath + "chemical_washer");
        addSoundEventWithSubtitle(MekanismSounds.COMBINER, basePath + "combiner");
        addSoundEventWithSubtitle(MekanismSounds.OSMIUM_COMPRESSOR, basePath + "compressor");
        addSoundEventWithSubtitle(MekanismSounds.CRUSHER, basePath + "crusher");
        addSoundEventWithSubtitle(MekanismSounds.ELECTROLYTIC_SEPARATOR, basePath + "electrolytic_separator");
        addSoundEventWithSubtitle(MekanismSounds.ENRICHMENT_CHAMBER, basePath + "enrichment_chamber");
        addSoundEventWithSubtitle(MekanismSounds.LASER, basePath + "laser");
        addSoundEventWithSubtitle(MekanismSounds.LOGISTICAL_SORTER, basePath + "logistical_sorter");
        addSoundEventWithSubtitle(MekanismSounds.METALLURGIC_INFUSER, basePath + "metallurgic_infuser");
        addSoundEventWithSubtitle(MekanismSounds.PRECISION_SAWMILL, basePath + "precision_sawmill");
        addSoundEventWithSubtitle(MekanismSounds.PRESSURIZED_REACTION_CHAMBER, basePath + "pressurized_reaction_chamber");
        addSoundEventWithSubtitle(MekanismSounds.PURIFICATION_CHAMBER, basePath + "purification_chamber");
        addSoundEventWithSubtitle(MekanismSounds.RESISTIVE_HEATER, basePath + "resistive_heater");
        addSoundEventWithSubtitle(MekanismSounds.ROTARY_CONDENSENTRATOR, basePath + "rotary_condensentrator");
        addSoundEventWithSubtitle(MekanismSounds.ENERGIZED_SMELTER, basePath + "energized_smelter");
        addSoundEventWithSubtitle(MekanismSounds.ISOTOPIC_CENTRIFUGE, basePath + "isotopic_centrifuge");
        addSoundEventWithSubtitle(MekanismSounds.NUTRITIONAL_LIQUIFIER, basePath + "nutritional_liquifier");
        addSoundEventWithSubtitle(MekanismSounds.INDUSTRIAL_ALARM, basePath + "industrial_alarm", sound -> sound.attenuationDistance(128));
        addSoundEventWithSubtitle(MekanismSounds.ANTIPROTONIC_NUCLEOSYNTHESIZER, basePath + "antiprotonic_nucleosynthesizer");
        addSoundEventWithSubtitle(MekanismSounds.PAINTING_MACHINE, basePath + "painting_machine");
        addSoundEventWithSubtitle(MekanismSounds.PIGMENT_EXTRACTOR, basePath + "pigment_extractor");
        addSoundEventWithSubtitle(MekanismSounds.PIGMENT_MIXER, basePath + "pigment_mixer");
        addSoundEventWithSubtitle(MekanismSounds.SPS, basePath + "sps");
    }

    private void addItemSoundEvents() {
        String basePath = "item/";
        addSoundEventWithSubtitle(MekanismSounds.HYDRAULIC, basePath + "hydraulic");
        addSoundEventWithSubtitle(MekanismSounds.FLAMETHROWER_IDLE, basePath + "flamethrower_idle");
        addSoundEventWithSubtitle(MekanismSounds.FLAMETHROWER_ACTIVE, basePath + "flamethrower_active");
        addSoundEventWithSubtitle(MekanismSounds.SCUBA_MASK, basePath + "scuba_mask");
        addSoundEventWithSubtitle(MekanismSounds.JETPACK, basePath + "jetpack");
        addSoundEventWithSubtitle(MekanismSounds.GRAVITATIONAL_MODULATION_UNIT, basePath + "gravitational_modulation_unit");

        addSoundEventWithSubtitle(MekanismSounds.GEIGER_SLOW, basePath + "geiger_slow");
        addSoundEventWithSubtitle(MekanismSounds.GEIGER_MEDIUM, basePath + "geiger_medium");
        addSoundEventWithSubtitle(MekanismSounds.GEIGER_ELEVATED, basePath + "geiger_elevated");
        addSoundEventWithSubtitle(MekanismSounds.GEIGER_FAST, basePath + "geiger_fast");
    }

    private void addHolidaySoundEvents() {
        String basePath = "holiday/";
        //Use the non holiday subtitles
        addSoundEvent(MekanismSounds.CHRISTMAS1, basePath + "nutcracker1", MekanismSounds.ENRICHMENT_CHAMBER);
        addSoundEvent(MekanismSounds.CHRISTMAS2, basePath + "nutcracker2", MekanismSounds.METALLURGIC_INFUSER);
        addSoundEvent(MekanismSounds.CHRISTMAS3, basePath + "nutcracker3", MekanismSounds.PURIFICATION_CHAMBER);
        addSoundEvent(MekanismSounds.CHRISTMAS4, basePath + "nutcracker4", MekanismSounds.ENERGIZED_SMELTER);
        addSoundEvent(MekanismSounds.CHRISTMAS5, basePath + "nutcracker5", MekanismSounds.CRUSHER);
    }

    private void addGuiSoundEvents() {
        String basePath = "gui/";
        //Manually call this to skip applying subtitles
        addSoundEvent(MekanismSounds.BEEP_ON, basePath + "beep_on", UnaryOperator.identity(), sound -> sound.pitch(0.8F));
        addSoundEvent(MekanismSounds.BEEP_OFF, basePath + "beep_off", UnaryOperator.identity(), sound -> sound.pitch(0.8F));
    }
}