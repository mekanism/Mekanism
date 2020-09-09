package mekanism.api.datagen.tag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class ChemicalTagsProvider<CHEMICAL extends Chemical<CHEMICAL>> extends ForgeRegistryTagsProvider<CHEMICAL> {

    private final String baseName;

    protected ChemicalTagsProvider(DataGenerator gen, IForgeRegistry<CHEMICAL> registry, String modid, @Nullable ExistingFileHelper existingFileHelper, String baseName) {
        super(gen, registry, modid, existingFileHelper);
        this.baseName = baseName;
    }

    @Nonnull
    @Override
    public String getName() {
        return baseName + " Tags: " + modId;
    }

    public abstract static class GasTagsProvider extends ChemicalTagsProvider<Gas> {

        protected GasTagsProvider(DataGenerator gen, String modid, @Nullable ExistingFileHelper existingFileHelper) {
            super(gen, MekanismAPI.gasRegistry(), modid, existingFileHelper, "Gas");
        }
    }

    public abstract static class InfuseTypeTagsProvider extends ChemicalTagsProvider<InfuseType> {

        protected InfuseTypeTagsProvider(DataGenerator gen, String modid, @Nullable ExistingFileHelper existingFileHelper) {
            super(gen, MekanismAPI.infuseTypeRegistry(), modid, existingFileHelper, "Infuse Type");
        }
    }

    public abstract static class PigmentTagsProvider extends ChemicalTagsProvider<Pigment> {

        protected PigmentTagsProvider(DataGenerator gen, String modid, @Nullable ExistingFileHelper existingFileHelper) {
            super(gen, MekanismAPI.pigmentRegistry(), modid, existingFileHelper, "Pigment");
        }
    }

    public abstract static class SlurryTagsProvider extends ChemicalTagsProvider<Slurry> {

        protected SlurryTagsProvider(DataGenerator gen, String modid, @Nullable ExistingFileHelper existingFileHelper) {
            super(gen, MekanismAPI.slurryRegistry(), modid, existingFileHelper, "Slurry");
        }
    }
}