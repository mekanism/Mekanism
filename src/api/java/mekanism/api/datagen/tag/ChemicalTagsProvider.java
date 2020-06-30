package mekanism.api.datagen.tag;

import java.nio.file.Path;
import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class ChemicalTagsProvider<CHEMICAL extends Chemical<CHEMICAL>> extends ForgeRegistryTagProvider<CHEMICAL> {

    private final String baseName;
    private final String path;

    protected ChemicalTagsProvider(DataGenerator gen, String modid, IForgeRegistry<CHEMICAL> registry, String baseName, String path) {
        super(gen, modid, registry);
        this.baseName = baseName;
        this.path = path;
    }

    @Nonnull
    @Override
    public String getName() {
        return baseName + " Tags: " + modid;
    }

    @Nonnull
    @Override
    protected Path makePath(ResourceLocation id) {
        return gen.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/" + path + "/" + id.getPath() + ".json");
    }

    public abstract static class GasTagsProvider extends ChemicalTagsProvider<Gas> {

        protected GasTagsProvider(DataGenerator gen, String modid) {
            super(gen, modid, MekanismAPI.gasRegistry(), "Gas", "gases");
        }
    }

    public abstract static class InfuseTypeTagsProvider extends ChemicalTagsProvider<InfuseType> {

        protected InfuseTypeTagsProvider(DataGenerator gen, String modid) {
            super(gen, modid, MekanismAPI.infuseTypeRegistry(), "Infuse Type", "infuse_types");
        }
    }

    public abstract static class PigmentTagsProvider extends ChemicalTagsProvider<Pigment> {

        protected PigmentTagsProvider(DataGenerator gen, String modid) {
            super(gen, modid, MekanismAPI.pigmentRegistry(), "Pigment", "pigments");
        }
    }

    public abstract static class SlurryTagsProvider extends ChemicalTagsProvider<Slurry> {

        protected SlurryTagsProvider(DataGenerator gen, String modid) {
            super(gen, modid, MekanismAPI.slurryRegistry(), "Slurry", "slurries");
        }
    }
}