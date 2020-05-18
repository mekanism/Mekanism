package mekanism.api.datagen.tag;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.infuse.InfuseType;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

public abstract class InfuseTypeTagsProvider extends ForgeRegistryTagProvider<InfuseType> {

    protected InfuseTypeTagsProvider(DataGenerator gen, String modid) {
        super(gen, modid, MekanismAPI.INFUSE_TYPE_REGISTRY);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Infuse Type Tags: " + modid;
    }

    @Override
    protected void setCollection(TagCollection<InfuseType> collection) {
        ChemicalTags.INFUSE_TYPE.setCollection(collection);
    }

    @Nonnull
    @Override
    protected Path makePath(ResourceLocation id) {
        return gen.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/infuse_types/" + id.getPath() + ".json");
    }
}