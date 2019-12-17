package mekanism.additions.common;

import mekanism.api.text.IHasTranslationKey;

public enum AdditionsLang implements IHasTranslationKey {
    CHANNEL("tooltip.mekanismadditions.channel"),
    WALKIE_DISABLED("tooltip.mekanismadditions.walkie_disabled");

    private final String key;

    AdditionsLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}