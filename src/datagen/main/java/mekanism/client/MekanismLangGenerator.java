package mekanism.client;

import mekanism.client.lang.BaseLanguageProvider;
import mekanism.common.Mekanism;
import net.minecraft.data.DataGenerator;

public class MekanismLangGenerator extends BaseLanguageProvider {

    public MekanismLangGenerator(DataGenerator gen) {
        super(gen, Mekanism.MODID);
    }

    @Override
    protected void addTranslations() {
        //TODO: Add things
    }
}