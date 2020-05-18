package mekanism.api.datagen.tag;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.pigment.Pigment;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

public abstract class PigmentTagsProvider extends ForgeRegistryTagProvider<Pigment> {

    protected PigmentTagsProvider(DataGenerator gen, String modid) {
        super(gen, modid, MekanismAPI.PIGMENT_REGISTRY);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Pigment Tags: " + modid;
    }

    @Override
    protected void setCollection(TagCollection<Pigment> collection) {
        ChemicalTags.PIGMENT.setCollection(collection);
    }

    @Nonnull
    @Override
    protected Path makePath(ResourceLocation id) {
        return gen.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/pigments/" + id.getPath() + ".json");
    }
}