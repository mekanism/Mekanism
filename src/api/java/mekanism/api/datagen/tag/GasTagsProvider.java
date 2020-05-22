package mekanism.api.datagen.tag;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import net.minecraft.data.DataGenerator;

public abstract class GasTagsProvider extends ChemicalTagsProvider<Gas> {

    protected GasTagsProvider(DataGenerator gen, String modid) {
        super(gen, modid, MekanismAPI.GAS_REGISTRY, "Gas", "gases", ChemicalTags.GAS);
    }
}