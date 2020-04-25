package mekanism.defense.common;

import mekanism.api.text.ILangEntry;
import net.minecraft.util.Util;

public enum DefenseLang implements ILangEntry {
    PLACEHOLDER("null", "null");

    private final String key;

    DefenseLang(String type, String path) {
        this(Util.makeTranslationKey(type, MekanismDefense.rl(path)));
    }

    DefenseLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
