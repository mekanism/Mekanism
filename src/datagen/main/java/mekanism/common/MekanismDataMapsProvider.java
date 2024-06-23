package mekanism.common;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPITags;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataMapTypes;
import mekanism.common.registries.MekanismGameEvents;
import net.minecraft.core.HolderLookup;
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
    protected void gather() {
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

        builder(NeoForgeDataMaps.FURNACE_FUELS)
              .add(MekanismBlocks.CHARCOAL_BLOCK.getId(), new FurnaceFuel(16_000), false)
        ;

        builder(MekanismDataMapTypes.MEKA_SUIT_ABSORPTION)
                .add(DamageTypes.SONIC_BOOM, new MekaSuitAbsorption(0.75f), false)
                .add(MekanismAPITags.DamageTypes.MEKASUIT_ALWAYS_SUPPORTED, new MekaSuitAbsorption(1f), false)
        ;
    }
}
