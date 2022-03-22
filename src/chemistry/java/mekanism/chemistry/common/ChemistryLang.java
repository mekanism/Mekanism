package mekanism.chemistry.common;

import mekanism.api.text.ILangEntry;
import net.minecraft.Util;

public enum ChemistryLang implements ILangEntry {
    DESCRIPTION_AIR_COMPRESSOR("description", "air_compressor");

    private final String key;

    ChemistryLang(String type, String path) {
        this(Util.makeDescriptionId(type, MekanismChemistry.rl(path)));
    }

    ChemistryLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
