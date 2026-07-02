package mekanism.additions.client.recipe_viewer.aliases;

import mekanism.additions.common.MekanismAdditions;
import mekanism.client.recipe_viewer.alias.IAliasedTranslation;

public enum AdditionsAliases implements IAliasedTranslation {
    GLOW_PANEL_LIGHT_SOURCE("glow_panel.light_source", "Light Source"),
    WALKIE_TALKIE_RADIO("walkie_talkie.radio", "Radio"),
    PLASTIC_ROAD_PATH("plastic_road.path", "Plastic Path"),
    ;

    private final String key;
    private final String alias;

    AdditionsAliases(String path, String alias) {
        this.key = MekanismAdditions.rl(path).toLanguageKey("alias");
        this.alias = alias;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String getAlias() {
        return alias;
    }
}