package mekanism.generators.client;

import mekanism.client.lang.BaseLanguageProvider;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.data.DataGenerator;

public class GeneratorsLangGenerator extends BaseLanguageProvider {

    public GeneratorsLangGenerator(DataGenerator gen) {
        super(gen, MekanismGenerators.MODID);
    }

    @Override
    protected void addTranslations() {
        //TODO: Add things, start with blocks/items and then look at the other lang entries
    }
}