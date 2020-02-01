package mekanism.generators.common;

import mekanism.common.tag.BaseTagProvider;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.registries.GeneratorsBlocks;
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
        addToTag(MekanismTags.Blocks.CARDBOARD_BLACKLIST,
              GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR,
              GeneratorsBlocks.WIND_GENERATOR
        );
    }
}