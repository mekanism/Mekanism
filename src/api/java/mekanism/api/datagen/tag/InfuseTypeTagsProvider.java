package mekanism.api.datagen.tag;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.infuse.InfuseType;
import net.minecraft.data.DataGenerator;

public abstract class InfuseTypeTagsProvider extends ChemicalTagsProvider<InfuseType> {

    protected InfuseTypeTagsProvider(DataGenerator gen, String modid) {
        super(gen, modid, MekanismAPI.INFUSE_TYPE_REGISTRY, "Infuse Type", "infuse_types", ChemicalTags.INFUSE_TYPE);
    }
}