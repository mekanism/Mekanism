package mekanism.generators.common;

import javax.annotation.Nullable;
import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsGases;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GeneratorsTagProvider extends BaseTagProvider {

    public GeneratorsTagProvider(DataGenerator gen, @Nullable ExistingFileHelper existingFileHelper) {
        super(gen, MekanismGenerators.MODID, existingFileHelper);
    }

    @Override
    protected void registerTags() {
        addBoxBlacklist();
    }

    private void addBoxBlacklist() {
        addFluids();
        addGases();
        addToTag(MekanismTags.Blocks.RELOCATION_NOT_SUPPORTED,
              GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
              GeneratorsBlocks.WIND_GENERATOR
        );
    }

    private void addFluids() {
        addToTag(GeneratorTags.Fluids.BIOETHANOL, GeneratorsFluids.BIOETHANOL);
        addToTag(GeneratorTags.Fluids.DEUTERIUM, GeneratorsFluids.DEUTERIUM);
        addToTag(GeneratorTags.Fluids.FUSION_FUEL, GeneratorsFluids.FUSION_FUEL);
        addToTag(GeneratorTags.Fluids.TRITIUM, GeneratorsFluids.TRITIUM);
    }

    private void addGases() {
        addToTag(GeneratorTags.Gases.DEUTERIUM, GeneratorsGases.DEUTERIUM);
        addToTag(GeneratorTags.Gases.TRITIUM, GeneratorsGases.TRITIUM);
        addToTag(GeneratorTags.Gases.FUSION_FUEL, GeneratorsGases.FUSION_FUEL);
    }
}