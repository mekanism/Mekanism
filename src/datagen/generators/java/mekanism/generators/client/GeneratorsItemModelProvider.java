package mekanism.generators.client;

import mekanism.client.model.BaseItemModelProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class GeneratorsItemModelProvider extends BaseItemModelProvider {

    public GeneratorsItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MekanismGenerators.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerBuckets(GeneratorsFluids.FLUIDS);
        registerModules(GeneratorsItems.ITEMS);
        registerGenerated(GeneratorsItems.HOHLRAUM, GeneratorsItems.SOLAR_PANEL, GeneratorsItems.TURBINE_BLADE);
    }
}