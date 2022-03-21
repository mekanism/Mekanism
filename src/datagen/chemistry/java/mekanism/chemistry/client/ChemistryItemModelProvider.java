package mekanism.chemistry.client;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.registries.ChemistryFluids;
import mekanism.chemistry.common.registries.ChemistryItems;
import mekanism.client.model.BaseItemModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ChemistryItemModelProvider extends BaseItemModelProvider {

    public ChemistryItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismChemistry.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerBuckets(ChemistryFluids.FLUIDS);
    }
}
