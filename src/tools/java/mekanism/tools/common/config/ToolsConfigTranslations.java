package mekanism.tools.common.config;

import mekanism.common.config.IConfigTranslation;
import mekanism.tools.common.MekanismTools;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum ToolsConfigTranslations implements IConfigTranslation {
    //SERVER_TOP_LEVEL("server", "Mekanism Defense Config. This config is synced between server and client."),
    ;

    private final String key;
    private final String translation;

    ToolsConfigTranslations(String path, String translation) {
        this.key = Util.makeDescriptionId("configuration", MekanismTools.rl(path));
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