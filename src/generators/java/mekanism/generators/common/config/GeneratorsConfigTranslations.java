package mekanism.generators.common.config;

import mekanism.common.config.IConfigTranslation;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum GeneratorsConfigTranslations implements IConfigTranslation {
    SERVER_TOP_LEVEL("server", "Mekanism Generators Config. This config is synced between server and client."),

    SERVER_ENERGY_PER_DT("server.energy_fusion_fuel", "Affects the Injection Rate, Max Temp, and Ignition Temp."),

    STORAGE_TOP_LEVEL("storage", "Generator Energy Storage Config. This config is synced from server to client."),

    GEAR_TOP_LEVEL("gear", "Mekanism Generators Gear Config. This config is synced from server to client."),

    GEAR_MEKA_SUIT("gear.meka_suit", "MekaSuit Settings"),
    GEAR_MEKA_SUIT_GEOTHERMAL("gear.meka_suit.geothermal_charging_rate", "Geothermal charging rate (Joules) of pants per tick, per degree above ambient, "
                                                                         + "per upgrade installed. This value scales down based on how much of the MekaSuit Pants is "
                                                                         + "submerged. Fire is treated as having a temperature of ~200K above ambient, lava has a "
                                                                         + "temperature of 1,000K above ambient."),
    GEAR_MEKA_SUIT_HEAT_DAMAGE("gear.meka_suit.heat_damage", "Percent of heat damage negated by MekaSuit Pants with maximum geothermal generator units "
                                                             + "installed. This number scales down linearly based on how many units are actually installed."),
    ;

    private final String key;
    private final String translation;

    GeneratorsConfigTranslations(String path, String translation) {
        this.key = Util.makeDescriptionId("configuration", MekanismGenerators.rl(path));
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