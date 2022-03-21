package mekanism.chemistry.client;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.client.model.BaseBlockModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ChemistryBlockModelProvider extends BaseBlockModelProvider {
    public ChemistryBlockModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MekanismChemistry.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
    }
}
