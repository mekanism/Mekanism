package mekanism.api.datagen.tag;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
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

    public abstract static class GasTagsProvider extends ChemicalTagsProvider<Gas> {

        protected GasTagsProvider(DataGenerator gen, String modid) {
            super(gen, modid, MekanismAPI.GAS_REGISTRY, "Gas", "gases", ChemicalTags.GAS);
        }
    }

    public abstract static class InfuseTypeTagsProvider extends ChemicalTagsProvider<InfuseType> {

        protected InfuseTypeTagsProvider(DataGenerator gen, String modid) {
            super(gen, modid, MekanismAPI.INFUSE_TYPE_REGISTRY, "Infuse Type", "infuse_types", ChemicalTags.INFUSE_TYPE);
        }
    }

    public abstract static class PigmentTagsProvider extends ChemicalTagsProvider<Pigment> {

        protected PigmentTagsProvider(DataGenerator gen, String modid) {
            super(gen, modid, MekanismAPI.PIGMENT_REGISTRY, "Pigment", "pigments", ChemicalTags.PIGMENT);
        }
    }

    public abstract static class SlurryTagsProvider extends ChemicalTagsProvider<Slurry> {

        protected SlurryTagsProvider(DataGenerator gen, String modid) {
            super(gen, modid, MekanismAPI.SLURRY_REGISTRY, "Slurry", "slurries", ChemicalTags.SLURRY);
        }
    }
}