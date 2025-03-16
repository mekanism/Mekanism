package mekanism.common;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.api.datamaps.chemical.ChemicalSolidTag;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity;
import mekanism.api.datamaps.chemical.attribute.CooledCoolant;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.VibrationFrequency;
import org.jetbrains.annotations.NotNull;

public class MekanismDataMapsProvider extends DataMapProvider {

    public static final long HYDROGEN_ENERGY_DENSITY = 200;

    public MekanismDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(@NotNull HolderLookup.Provider provider) {
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

        Builder<ChemicalSolidTag, Chemical> chemicalSolidTagBuilder = builder(IMekanismDataMapTypes.INSTANCE.chemicalSolidTag());
        for (Map.Entry<PrimaryResource, SlurryRegistryObject<Chemical, Chemical>> entry : MekanismChemicals.PROCESSED_RESOURCES.entrySet()) {
            chemicalSolidTagBuilder.add(entry.getValue().getCleanSlurry(), new ChemicalSolidTag(entry.getKey().getOreTag()), false);
        }

        builder(IMekanismDataMapTypes.INSTANCE.chemicalFuel())
              //GENERAL_ENERGY_CONVERSION_HYDROGEN("general.energy_conversion.hydrogen", "Hydrogen Energy Density",
              //"How much energy is produced per mB of Hydrogen, also affects Electrolytic Separator usage, Ethene burn rate and Gas-Burning Generator energy capacity."),
              .add(MekanismChemicals.HYDROGEN, new ChemicalFuel(1, HYDROGEN_ENERGY_DENSITY), false)
              .add(MekanismChemicals.ETHENE, new ChemicalFuel(getEtheneBurnTime(), getEtheneEnergyPerTick()), false)
        ;

        builder(IMekanismDataMapTypes.INSTANCE.chemicalRadioactivity())
              .add(MekanismChemicals.NUCLEAR_WASTE, new ChemicalRadioactivity(0.01), false)
              .add(MekanismChemicals.SPENT_NUCLEAR_WASTE, new ChemicalRadioactivity(0.01), false)
              .add(MekanismChemicals.PLUTONIUM, new ChemicalRadioactivity(0.02), false)
              .add(MekanismChemicals.POLONIUM, new ChemicalRadioactivity(0.05), false)
        ;

        builder(IMekanismDataMapTypes.INSTANCE.cooledChemicalCoolant())
              .add(MekanismChemicals.SODIUM, new CooledCoolant(MekanismChemicals.SUPERHEATED_SODIUM, 5, 1), false)
        ;
        builder(IMekanismDataMapTypes.INSTANCE.heatedChemicalCoolant())
              .add(MekanismChemicals.SUPERHEATED_SODIUM, new HeatedCoolant(MekanismChemicals.SODIUM, 5), false)
        ;
    }

    private static long getEtheneEnergyPerTick() {
        long bioGeneration = 350;//Default bio generator value
        long energy = Math.multiplyExact(40, Math.multiplyExact(2, bioGeneration));
        return Math.addExact(energy, HYDROGEN_ENERGY_DENSITY) / getEtheneBurnTime();
    }

    private static int getEtheneBurnTime() {
        return 2 * SharedConstants.TICKS_PER_SECOND;
    }
}
