package mekanism.api.datagen.tag;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class ChemicalTagsProvider<CHEMICAL extends Chemical<CHEMICAL>> extends ForgeRegistryTagProvider<CHEMICAL> {

    private final String baseName;
    private final String path;
    private final ChemicalTags<CHEMICAL> tags;

    protected ChemicalTagsProvider(DataGenerator gen, String modid, IForgeRegistry<CHEMICAL> registry, String baseName, String path, ChemicalTags<CHEMICAL> tags) {
        super(gen, modid, registry);
        this.baseName = baseName;
        this.path = path;
        this.tags = tags;
    }

    @Nonnull
    @Override
    public String getName() {
        return baseName + " Tags: " + modid;
    }

    @Override
    protected void setCollection(TagCollection<CHEMICAL> collection) {
        tags.setCollection(collection);
    }

    @Nonnull
    @Override
    protected Path makePath(ResourceLocation id) {
        return gen.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/" + path + "/" + id.getPath() + ".json");
    }
}