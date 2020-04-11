package mekanism.client.model;

import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismFluids;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class MekanismItemModelProvider extends BaseItemModelProvider {

    public MekanismItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Mekanism.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Buckets
        MekanismFluids.FLUIDS.getAllFluids().forEach(this::registerBucket);
    }
}