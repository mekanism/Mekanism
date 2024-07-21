package mekanism.defense.common.config;

import mekanism.common.config.IConfigTranslation;
import mekanism.defense.common.MekanismDefense;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum DefenseConfigTranslations implements IConfigTranslation {
    SERVER_TOP_LEVEL("server", "Mekanism Defense Config. This config is synced between server and client."),
    ;

    private final String key;
    private final String translation;

    DefenseConfigTranslations(String path, String translation) {
        this.key = Util.makeDescriptionId("configuration", MekanismDefense.rl(path));
        this.translation = translation;
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String translation() {
        return translation;
    }


}