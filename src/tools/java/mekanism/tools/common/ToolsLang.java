package mekanism.tools.common;

import mekanism.api.text.IHasTranslationKey;

public enum ToolsLang implements IHasTranslationKey {
    HP("tooltip.mekanismtools.hp");

    private final String key;

    ToolsLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}