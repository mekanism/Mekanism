package mekanism.client.state;

import mekanism.client.model.MekanismBlockModelProvider;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismFluids;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class MekanismBlockStateProvider extends BaseBlockStateProvider<MekanismBlockModelProvider> {

    public MekanismBlockStateProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Mekanism.MODID, existingFileHelper, MekanismBlockModelProvider::new);
    }

    @Override
    protected void registerStatesAndModels() {
        registerFluidBlockStates(MekanismFluids.FLUIDS.getAllFluids());
    }
}