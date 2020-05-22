package mekanism.api.datagen.tag;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.pigment.Pigment;
import net.minecraft.data.DataGenerator;

public abstract class PigmentTagsProvider extends ChemicalTagsProvider<Pigment> {

    protected PigmentTagsProvider(DataGenerator gen, String modid) {
        super(gen, modid, MekanismAPI.PIGMENT_REGISTRY, "Pigment", "pigments", ChemicalTags.PIGMENT);
    }
}