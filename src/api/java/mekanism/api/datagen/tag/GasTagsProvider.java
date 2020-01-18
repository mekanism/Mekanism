package mekanism.api.datagen.tag;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

public abstract class GasTagsProvider extends ForgeRegistryTagProvider<Gas> {

    protected GasTagsProvider(DataGenerator gen, String modid) {
        super(gen, modid, MekanismAPI.GAS_REGISTRY);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Gas Tags: " + modid;
    }

    @Override
    protected void setCollection(TagCollection<Gas> collection) {
        GasTags.setCollection(collection);
    }

    @Nonnull
    @Override
    protected Path makePath(ResourceLocation id) {
        return gen.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/gases/" + id.getPath() + ".json");
    }
}