package mekanism.defense.client;

import mekanism.client.lang.BaseLanguageProvider;
import mekanism.defense.common.MekanismDefense;
import net.minecraft.data.DataGenerator;

public class DefenseLangProvider extends BaseLanguageProvider {

    public DefenseLangProvider(DataGenerator gen) {
        super(gen, MekanismDefense.MODID);
    }

    @Override
    protected void addTranslations() {
    }
}