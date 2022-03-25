package mekanism.chemistry.common;

import mekanism.api.text.ILangEntry;
import net.minecraft.Util;

public enum ChemistryLang implements ILangEntry {
    DISTILLER("distiller", "fractionating_distiller"),
    DESCRIPTION_FRACTIONATING_DISTILLER_BLOCK("description", "fractionating_distiller_block"),
    DESCRIPTION_FRACTIONATING_DISTILLER_VALVE("description", "fractionating_distiller_valve"),
    DESCRIPTION_FRACTIONATING_DISTILLER_CONTROLLER("description", "fractionating_distiller_controller"),
    DESCRIPTION_AIR_COMPRESSOR("description", "air_compressor.json");

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
