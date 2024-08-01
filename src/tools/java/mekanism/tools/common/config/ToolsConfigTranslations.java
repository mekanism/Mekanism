package mekanism.tools.common.config;

import mekanism.common.config.IConfigTranslation;
import mekanism.tools.common.MekanismTools;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum ToolsConfigTranslations implements IConfigTranslation {
    //SERVER_TOP_LEVEL("server", "Mekanism Defense Config. This config is synced between server and client."),
    ;

    private final String key;
    private final String title;
    private final String tooltip;

    ToolsConfigTranslations(String path, String title, String tooltip) {
        this.key = Util.makeDescriptionId("configuration", MekanismTools.rl(path));
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