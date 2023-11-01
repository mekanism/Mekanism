package mekanism.generators.client;

import mekanism.client.model.BaseBlockModelProvider;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class GeneratorsBlockModelProvider extends BaseBlockModelProvider {

    public GeneratorsBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MekanismGenerators.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

    }
}