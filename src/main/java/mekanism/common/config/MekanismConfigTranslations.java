package mekanism.common.config;

import mekanism.common.Mekanism;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum MekanismConfigTranslations implements IConfigTranslation {
    //SERVER_TOP_LEVEL("server", "Mekanism Defense Config. This config is synced between server and client."),


    BASE_ENERGY_STORAGE_JOULES("storage.energy.base", "Base energy storage", "Base energy storage (Joules)."),
    ;

    private final String key;
    private final String title;
    private final String tooltip;

    MekanismConfigTranslations(String path, String title, String tooltip) {
        this.key = Util.makeDescriptionId("configuration", Mekanism.rl(path));
        this.title = title;
        this.tooltip = tooltip;
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String tooltip() {
        return tooltip;
    }


}