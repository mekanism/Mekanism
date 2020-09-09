package mekanism.generators.client;

import mekanism.client.state.BaseBlockStateProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsFluids;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GeneratorsBlockStateProvider extends BaseBlockStateProvider<GeneratorsBlockModelProvider> {

    public GeneratorsBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismGenerators.MODID, existingFileHelper, GeneratorsBlockModelProvider::new);
    }

    @Override
    protected void registerStatesAndModels() {
        registerFluidBlockStates(GeneratorsFluids.FLUIDS.getAllFluids());
    }
}