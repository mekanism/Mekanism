package mekanism.generators.client;

import mekanism.client.model.BaseItemModelProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsFluids;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class GeneratorsItemModelProvider extends BaseItemModelProvider {

    public GeneratorsItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismGenerators.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Buckets
        GeneratorsFluids.FLUIDS.getAllFluids().forEach(this::registerBucket);
    }
}