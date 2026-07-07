package mekanism.generators.common;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPITags;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.generators.common.registries.GeneratorsModules;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;

public class GeneratorsDataMapsProvider extends DataMapProvider {

    public GeneratorsDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        builder(IMekanismDataMapTypes.INSTANCE.supportedModules())
              .add(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_HELMET, HolderSet.direct(
                    GeneratorsModules.SOLAR_RECHARGING_UNIT
              ), false)
              .add(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR_LEGGINGS, HolderSet.direct(
                    GeneratorsModules.GEOTHERMAL_GENERATOR_UNIT
              ), false)
        ;
    }
}
