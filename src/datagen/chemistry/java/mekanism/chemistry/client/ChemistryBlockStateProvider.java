package mekanism.chemistry.client;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.registries.ChemistryFluids;
import mekanism.client.state.BaseBlockStateProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ChemistryBlockStateProvider extends BaseBlockStateProvider<ChemistryBlockModelProvider> {

    public ChemistryBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismChemistry.MODID, existingFileHelper, ChemistryBlockModelProvider::new);
    }

    @Override
    protected void registerStatesAndModels() {
        registerFluidBlockStates(ChemistryFluids.FLUIDS.getAllFluids());
    }
}
