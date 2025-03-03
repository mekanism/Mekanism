package mekanism.common;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPITags;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataMapTypes;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismItems;
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

        builder(MekanismDataMapTypes.MEKA_SUIT_ABSORPTION)
                .add(DamageTypes.SONIC_BOOM, new MekaSuitAbsorption(0.75f), false)
                .add(MekanismAPITags.DamageTypes.MEKASUIT_ALWAYS_SUPPORTED, new MekaSuitAbsorption(1f), false)
        ;
    }
}
