package mekanism.defense.client;

import mekanism.client.lang.BaseLanguageProvider;
import mekanism.defense.common.DefenseLang;
import mekanism.defense.common.MekanismDefense;
import net.minecraft.data.PackOutput;

public class DefenseLangProvider extends BaseLanguageProvider {

    public DefenseLangProvider(PackOutput output) {
        super(output, MekanismDefense.MODID, MekanismDefense.instance);
    }

    @Override
    protected void addTranslations() {
        addMisc();
    }

    private void addMisc() {
        addPackData(DefenseLang.MEKANISM_DEFENSE, DefenseLang.PACK_DESCRIPTION);
    }
}