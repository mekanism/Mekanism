package mekanism.generators.client;

import mekanism.client.state.BaseBlockStateProvider;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsFluids;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class GeneratorsBlockStateProvider extends BaseBlockStateProvider<GeneratorsBlockModelProvider> {

    public GeneratorsBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MekanismGenerators.MODID, existingFileHelper, GeneratorsBlockModelProvider::new);
    }

    @Override
    protected void registerStatesAndModels() {
        registerFluidBlockStates(GeneratorsFluids.FLUIDS);
    }
}