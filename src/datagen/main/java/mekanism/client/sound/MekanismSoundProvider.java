package mekanism.client.sound;

import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class MekanismSoundProvider extends BaseSoundProvider {

    public MekanismSoundProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, existingFileHelper, Mekanism.MODID);
    }

    @Override
    protected void addSoundEvents() {
        addTileSoundEvents();
        addItemSoundEvents();
        addHolidaySoundEvents();
        addGuiSoundEvents();
    }

    private void addTileSoundEvents() {
        String basePath = "tile/";
        addSoundEventWithSubtitle(MekanismSounds.CHARGEPAD, Mekanism.rl(basePath + "chargepad"));
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_CRYSTALLIZER, Mekanism.rl(basePath + "chemical_crystallizer"));
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_DISSOLUTION_CHAMBER, Mekanism.rl(basePath + "chemical_dissolution_chamber"));
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_INFUSER, Mekanism.rl(basePath + "chemical_infuser"));
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_INJECTION_CHAMBER, Mekanism.rl(basePath + "chemical_injection_chamber"));
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_OXIDIZER, Mekanism.rl(basePath + "chemical_oxidizer"));
        addSoundEventWithSubtitle(MekanismSounds.CHEMICAL_WASHER, Mekanism.rl(basePath + "chemical_washer"));
        addSoundEventWithSubtitle(MekanismSounds.COMBINER, Mekanism.rl(basePath + "combiner"));
        addSoundEventWithSubtitle(MekanismSounds.OSMIUM_COMPRESSOR, Mekanism.rl(basePath + "compressor"));
        addSoundEventWithSubtitle(MekanismSounds.CRUSHER, Mekanism.rl(basePath + "crusher"));
        addSoundEventWithSubtitle(MekanismSounds.ELECTROLYTIC_SEPARATOR, Mekanism.rl(basePath + "electrolytic_separator"));
        addSoundEventWithSubtitle(MekanismSounds.ENRICHMENT_CHAMBER, Mekanism.rl(basePath + "enrichment_chamber"));
        addSoundEventWithSubtitle(MekanismSounds.LASER, Mekanism.rl(basePath + "laser"));
        addSoundEventWithSubtitle(MekanismSounds.LOGISTICAL_SORTER, Mekanism.rl(basePath + "logistical_sorter"));
        addSoundEventWithSubtitle(MekanismSounds.METALLURGIC_INFUSER, Mekanism.rl(basePath + "metallurgic_infuser"));
        addSoundEventWithSubtitle(MekanismSounds.PRECISION_SAWMILL, Mekanism.rl(basePath + "precision_sawmill"));
        addSoundEventWithSubtitle(MekanismSounds.PRESSURIZED_REACTION_CHAMBER, Mekanism.rl(basePath + "pressurized_reaction_chamber"));
        addSoundEventWithSubtitle(MekanismSounds.PURIFICATION_CHAMBER, Mekanism.rl(basePath + "purification_chamber"));
        addSoundEventWithSubtitle(MekanismSounds.RESISTIVE_HEATER, Mekanism.rl(basePath + "resistive_heater"));
        addSoundEventWithSubtitle(MekanismSounds.ROTARY_CONDENSENTRATOR, Mekanism.rl(basePath + "rotary_condensentrator"));
        addSoundEventWithSubtitle(MekanismSounds.ENERGIZED_SMELTER, Mekanism.rl(basePath + "energized_smelter"));
        addSoundEventWithSubtitle(MekanismSounds.ISOTOPIC_CENTRIFUGE, Mekanism.rl(basePath + "isotopic_centrifuge"));
        addSoundEventWithSubtitle(MekanismSounds.NUTRITIONAL_LIQUIFIER, Mekanism.rl(basePath + "nutritional_liquifier"));
        addSoundEventWithSubtitle(MekanismSounds.INDUSTRIAL_ALARM, Mekanism.rl(basePath + "industrial_alarm"));
        addSoundEventWithSubtitle(MekanismSounds.ANTIPROTONIC_NUCLEOSYNTHESIZER, Mekanism.rl(basePath + "antiprotonic_nucleosynthesizer"));
        addSoundEventWithSubtitle(MekanismSounds.SPS, Mekanism.rl(basePath + "sps"));
    }

    private void addItemSoundEvents() {
        String basePath = "item/";
        addSoundEventWithSubtitle(MekanismSounds.HYDRAULIC, Mekanism.rl(basePath + "hydraulic"));
        addSoundEventWithSubtitle(MekanismSounds.FLAMETHROWER_IDLE, Mekanism.rl(basePath + "flamethrower_idle"));
        addSoundEventWithSubtitle(MekanismSounds.FLAMETHROWER_ACTIVE, Mekanism.rl(basePath + "flamethrower_active"));
        addSoundEventWithSubtitle(MekanismSounds.SCUBA_MASK, Mekanism.rl(basePath + "scuba_mask"));
        addSoundEventWithSubtitle(MekanismSounds.JETPACK, Mekanism.rl(basePath + "jetpack"));
        addSoundEventWithSubtitle(MekanismSounds.GRAVITATIONAL_MODULATION_UNIT, Mekanism.rl(basePath + "gravitational_modulation_unit"));

        addSoundEventWithSubtitle(MekanismSounds.GEIGER_SLOW, Mekanism.rl(basePath + "geiger_slow"));
        addSoundEventWithSubtitle(MekanismSounds.GEIGER_MEDIUM, Mekanism.rl(basePath + "geiger_medium"));
        addSoundEventWithSubtitle(MekanismSounds.GEIGER_ELEVATED, Mekanism.rl(basePath + "geiger_elevated"));
        addSoundEventWithSubtitle(MekanismSounds.GEIGER_FAST, Mekanism.rl(basePath + "geiger_fast"));
    }

    private void addHolidaySoundEvents() {
        String basePath = "holiday/";
        //Use the non holiday subtitles
        addSoundEvent(MekanismSounds.CHRISTMAS1, Mekanism.rl(basePath + "nutcracker1"), MekanismSounds.ENRICHMENT_CHAMBER);
        addSoundEvent(MekanismSounds.CHRISTMAS2, Mekanism.rl(basePath + "nutcracker2"), MekanismSounds.METALLURGIC_INFUSER);
        addSoundEvent(MekanismSounds.CHRISTMAS3, Mekanism.rl(basePath + "nutcracker3"), MekanismSounds.PURIFICATION_CHAMBER);
        addSoundEvent(MekanismSounds.CHRISTMAS4, Mekanism.rl(basePath + "nutcracker4"), MekanismSounds.ENERGIZED_SMELTER);
        addSoundEvent(MekanismSounds.CHRISTMAS5, Mekanism.rl(basePath + "nutcracker5"), MekanismSounds.CRUSHER);
    }

    private void addGuiSoundEvents() {
        String basePath = "gui/";
        addSoundEvent(MekanismSounds.BEEP, Mekanism.rl(basePath + "beep"));
    }
}