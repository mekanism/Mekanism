package mekanism.tools.common;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.ILangEntry;
import net.minecraft.Util;

@NothingNullByDefault
public enum ToolsLang implements ILangEntry {
    MEKANISM_TOOLS("constants", "mod_name"),
    PACK_DESCRIPTION("constants", "pack_description"),
    HP("tooltip", "hp");

    private final String key;

    ToolsLang(String type, String path) {
        this(Util.makeDescriptionId(type, MekanismTools.rl(path)));
    }

    ToolsLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}