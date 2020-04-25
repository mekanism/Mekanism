package mekanism.additions.common;

import mekanism.api.text.ILangEntry;
import net.minecraft.util.Util;

public enum AdditionsLang implements ILangEntry {
    CHANNEL("walkie", "channel"),
    CHANNEL_CHANGE("walkie", "channel.change"),
    WALKIE_DISABLED("walkie", "disabled"),
    KEY_VOICE("key", "voice"),
    DESCRIPTION_OBSIDIAN_TNT("description", "obsidian_tnt"),
    DESCRIPTION_GLOW_PANEL("description", "glow_panel");

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