package mekanism.generators.common.config;

import mekanism.common.config.IConfigTranslation;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum GeneratorsConfigTranslations implements IConfigTranslation {
    SERVER_TOP_LEVEL("server", "Mekanism Generators Config", "Mekanism Generators Config. This config is synced between server and client."),

    SERVER_ENERGY_PER_DT("server.energy_fusion_fuel", "Energy Per D-T Fuel", "Affects the Injection Rate, Max Temp, and Ignition Temp."),

    STORAGE_TOP_LEVEL("storage", "Generator Energy Storage Config", "Generator Energy Storage Config. This config is synced from server to client."),

    GEAR_TOP_LEVEL("gear", "Mekanism Generators Gear Config", "Mekanism Generators Gear Config. This config is synced from server to client."),

    GEAR_MEKA_SUIT("gear.meka_suit", "MekaSuit Settings", "Settings for configuring the MekaSuit"),
    GEAR_MEKA_SUIT_GEOTHERMAL("gear.meka_suit.geothermal_charging_rate", "Geothermal charging rate",
          "Geothermal charging rate (Joules) of pants per tick, per degree above ambient, per upgrade installed. This value scales down based on how much of "
          + "the MekaSuit Pants is submerged. Fire is treated as having a temperature of ~200K above ambient, lava has a temperature of 1,000K above ambient."),
    GEAR_MEKA_SUIT_HEAT_DAMAGE("gear.meka_suit.heat_damage", "Heat damage reduction",
          "Percent of heat damage negated by MekaSuit Pants with maximum geothermal generator units installed. This number scales down linearly based on how many "
          + "units are actually installed."),
    ;

    private final String key;
    private final String title;
    private final String tooltip;

    GeneratorsConfigTranslations(String path, String title, String tooltip) {
        this.key = Util.makeDescriptionId("configuration", MekanismGenerators.rl(path));
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