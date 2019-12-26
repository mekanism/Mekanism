package mekanism.additions.common;

import mekanism.common.base.ILangEntry;
import net.minecraft.util.Util;

public enum AdditionsLang implements ILangEntry {
    CHANNEL("tooltip", "channel"),
    WALKIE_DISABLED("tooltip", "walkie_disabled");

    private final String key;

    AdditionsLang(String type, String path) {
        this(Util.makeTranslationKey(type, MekanismAdditions.rl(path)));
    }

    AdditionsLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}