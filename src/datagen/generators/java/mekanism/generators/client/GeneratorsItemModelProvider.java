package mekanism.generators.client;

import mekanism.client.model.BaseItemModelProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GeneratorsItemModelProvider extends BaseItemModelProvider {

    public GeneratorsItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismGenerators.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerBuckets(GeneratorsFluids.FLUIDS);
        registerModules(GeneratorsItems.ITEMS);
        registerGenerated(GeneratorsItems.HOHLRAUM, GeneratorsItems.SOLAR_PANEL, GeneratorsItems.TURBINE_BLADE);
    }
}