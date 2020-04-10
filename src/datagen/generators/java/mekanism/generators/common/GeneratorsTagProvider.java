package mekanism.generators.common;

import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsGases;
import net.minecraft.data.DataGenerator;

public class GeneratorsTagProvider extends BaseTagProvider {

    public GeneratorsTagProvider(DataGenerator gen) {
        super(gen, MekanismGenerators.MODID);
    }

    @Override
    protected void registerTags() {
        addBoxBlacklist();
    }

    private void addBoxBlacklist() {
        addFluids();
        addGases();
        addToTag(MekanismTags.Blocks.CARDBOARD_BLACKLIST,
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