package mekanism.defense.common.config;

import mekanism.common.config.IConfigTranslation;
import mekanism.defense.common.MekanismDefense;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum DefenseConfigTranslations implements IConfigTranslation {
    ;

    private final String key;
    private final String title;
    private final String tooltip;

    DefenseConfigTranslations(String path, String title, String tooltip) {
        this.key = Util.makeDescriptionId("configuration", MekanismDefense.rl(path));
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