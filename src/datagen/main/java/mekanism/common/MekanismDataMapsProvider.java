package mekanism.common;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.CleanDirtySlurryId;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.api.datamaps.chemical.ChemicalSolidTag;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity;
import mekanism.api.datamaps.chemical.attribute.CooledCoolant;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.util.ChemicalUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.PackOutput;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.VibrationFrequency;

public class MekanismDataMapsProvider extends DataMapProvider {

    public MekanismDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(NeoForgeDataMaps.VIBRATION_FREQUENCIES)
              //Follows vanilla's logic for what gives what sort of frequency
              // A frequency of four is used for gliding with an elytra or unique mob actions
              .add(MekanismGameEvents.JETPACK_BURN, new VibrationFrequency(4), false)
              .add(MekanismGameEvents.GRAVITY_MODULATE, new VibrationFrequency(4), false)
              //Note: We use 5 for boosted modulation to be able to tell it apart easier from normal modulating
              .add(MekanismGameEvents.GRAVITY_MODULATE_BOOSTED, new VibrationFrequency(5), false)
              // A frequency of ten is for blocks activating
              .add(MekanismGameEvents.SEISMIC_VIBRATION, new VibrationFrequency(10), false)
        ;

        int bioFuelBurnTime = 5 * SharedConstants.TICKS_PER_SECOND;
        builder(NeoForgeDataMaps.FURNACE_FUELS)
              .add(MekanismBlocks.CHARCOAL_BLOCK.getId(), new FurnaceFuel(16_000), false)
              .add(MekanismItems.BIO_FUEL.getId(), new FurnaceFuel(bioFuelBurnTime), false)
              //Note: Similar to how vanilla handles coal -> coal block burn times, we multiply by 10 instead of by 9
              // so that you get a little bit more bang for your buck
              .add(MekanismBlocks.BIO_FUEL_BLOCK.getId(), new FurnaceFuel(10 * bioFuelBurnTime), false)
        ;

        builder(IMekanismDataMapTypes.INSTANCE.mekaSuitAbsorption())
              .add(DamageTypes.SONIC_BOOM, new MekaSuitAbsorption(0.75F), false)
              .add(MekanismAPITags.DamageTypes.MEKASUIT_ALWAYS_SUPPORTED, new MekaSuitAbsorption(1F), false)
        ;

        gatherSupportedUnits();

        Builder<ChemicalSolidTag, Chemical> chemicalSolidTagBuilder = builder(IMekanismDataMapTypes.INSTANCE.chemicalSolidTag());
        for (Map.Entry<PrimaryResource, CleanDirtySlurryId> entry : MekanismChemicals.PROCESSED_RESOURCES.entrySet()) {
            chemicalSolidTagBuilder.add(entry.getValue().clean(), new ChemicalSolidTag(entry.getKey().getOreTag()), false);
        }

        builder(IMekanismDataMapTypes.INSTANCE.chemicalFuel())
              //GENERAL_ENERGY_CONVERSION_HYDROGEN("general.energy_conversion.hydrogen", "Hydrogen Energy Density",
              //"How much energy is produced per mB of Hydrogen, also affects Electrolytic Separator usage, Ethene burn rate and Gas-Burning Generator energy capacity."),
              .add(ChemicalIds.HYDROGEN, new ChemicalFuel(10, ChemicalUtils.DEFAULT_HYDROGEN_ENERGY_DENSITY), false)
              .add(ChemicalIds.ETHENE, new ChemicalFuel(10, 8), false)
        ;

        builder(IMekanismDataMapTypes.INSTANCE.chemicalRadioactivity())
              .add(ChemicalIds.NUCLEAR_WASTE, new ChemicalRadioactivity(0.01), false)
              .add(ChemicalIds.SPENT_NUCLEAR_WASTE, new ChemicalRadioactivity(0.01), false)
              .add(ChemicalIds.PLUTONIUM, new ChemicalRadioactivity(0.02), false)
              .add(ChemicalIds.POLONIUM, new ChemicalRadioactivity(0.05), false)
        ;

        builder(IMekanismDataMapTypes.INSTANCE.cooledChemicalCoolant())
              .add(ChemicalIds.SODIUM, new CooledCoolant(provider.getOrThrow(ChemicalIds.SUPERHEATED_SODIUM), 5, 1), false)
        ;
        builder(IMekanismDataMapTypes.INSTANCE.heatedChemicalCoolant())
              .add(ChemicalIds.SUPERHEATED_SODIUM, new HeatedCoolant(provider.getOrThrow(ChemicalIds.SODIUM), 5), false)
        ;
    }

    private void gatherSupportedUnits() {
        //TODO - 26.2: Expose resource keys for the various builtin meka module containers
        builder(IMekanismDataMapTypes.INSTANCE.supportedModules())
              .add(MekanismAPITags.Items.MODULE_CONTAINERS, HolderSet.direct(
                    MekanismModules.ENERGY_UNIT
              ), false)
              //MekaSuit Common
              .add(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR, HolderSet.direct(
                    MekanismModules.COLOR_MODULATION_UNIT,
                    MekanismModules.LASER_DISSIPATION_UNIT,
                    MekanismModules.RADIATION_SHIELDING_UNIT
              ), false)
              //Meka-Tool
              .add(MekanismAPITags.Items.MODULE_CONTAINERS_MEKA_TOOL, HolderSet.direct(
                    MekanismModules.ATTACK_AMPLIFICATION_UNIT,
                    MekanismModules.BLASTING_UNIT,
                    MekanismModules.EXCAVATION_ESCALATION_UNIT,
                    MekanismModules.FARMING_UNIT,
                    MekanismModules.FORTUNE_UNIT,
                    MekanismModules.SHEARING_UNIT,
                    MekanismModules.SILK_TOUCH_UNIT,
                    MekanismModules.TELEPORTATION_UNIT,
                    MekanismModules.VEIN_MINING_UNIT
              ), false)
              //MekaSuit Helmet
              .add(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_HELMET, HolderSet.direct(
                    MekanismModules.ELECTROLYTIC_BREATHING_UNIT,
                    MekanismModules.INHALATION_PURIFICATION_UNIT,
                    MekanismModules.NUTRITIONAL_INJECTION_UNIT,
                    MekanismModules.VISION_ENHANCEMENT_UNIT
              ), false)
              //MekaSuit Chestplate
              .add(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_CHESTPLATE, HolderSet.direct(
                    MekanismModules.CHARGE_DISTRIBUTION_UNIT,
                    MekanismModules.DOSIMETER_UNIT,
                    MekanismModules.ELYTRA_UNIT,
                    MekanismModules.GEIGER_UNIT,
                    MekanismModules.GRAVITATIONAL_MODULATING_UNIT,
                    MekanismModules.JETPACK_UNIT
              ), false)
              //MekaSuit Leggings
              .add(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_LEGGINGS, HolderSet.direct(
                    MekanismModules.GYROSCOPIC_STABILIZATION_UNIT,
                    MekanismModules.HYDROSTATIC_REPULSOR_UNIT,
                    MekanismModules.LOCOMOTIVE_BOOSTING_UNIT,
                    MekanismModules.MOTORIZED_SERVO_UNIT
              ), false)
              //MekaSuit Boots
              .add(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_BOOTS, HolderSet.direct(
                    MekanismModules.FROST_WALKER_UNIT,
                    MekanismModules.HYDRAULIC_PROPULSION_UNIT,
                    MekanismModules.MAGNETIC_ATTRACTION_UNIT,
                    MekanismModules.SOUL_SURFER_UNIT
              ), false)
        ;
    }
}
