package mekanism.generators.client;

import mekanism.client.model.BaseBlockModelProvider;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GeneratorsBlockModelProvider extends BaseBlockModelProvider {

    public GeneratorsBlockModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismGenerators.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}