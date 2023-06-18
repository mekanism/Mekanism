package mekanism.defense.client;

import mekanism.client.lang.BaseLanguageProvider;
import mekanism.defense.common.DefenseLang;
import mekanism.defense.common.MekanismDefense;
import net.minecraft.data.PackOutput;

public class DefenseLangProvider extends BaseLanguageProvider {

    public DefenseLangProvider(PackOutput output) {
        super(output, MekanismDefense.MODID);
    }

    @Override
    protected void addTranslations() {
        add(DefenseLang.PACK_DESCRIPTION, "Resources used for Mekanism: Defense");
    }
}