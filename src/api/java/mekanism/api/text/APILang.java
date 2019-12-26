package mekanism.api.text;

import mekanism.api.MekanismAPI;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

/**
 * Lang entries declared in the API and provided by Mekanism.
 *
 * @apiNote These should only be accessed via their corresponding users, except for use in making it easier to not miss any entries in the DataGenerators
 */
public enum APILang implements IHasTranslationKey {
    //Upgrades
    UPGRADE_SPEED("upgrade", "speed"),
    UPGRADE_SPEED_DESCRIPTION("upgrade", "speed.description"),
    UPGRADE_ENERGY("upgrade", "energy"),
    UPGRADE_ENERGY_DESCRIPTION("upgrade", "energy.description"),
    UPGRADE_FILTER("upgrade", "filter"),
    UPGRADE_FILTER_DESCRIPTION("upgrade", "filter.description"),
    UPGRADE_GAS("upgrade", "gas"),
    UPGRADE_GAS_DESCRIPTION("upgrade", "gas.description"),
    UPGRADE_MUFFLING("upgrade", "muffling"),
    UPGRADE_MUFFLING_DESCRIPTION("upgrade", "muffling.description"),
    UPGRADE_ANCHOR("upgrade", "anchor"),
    UPGRADE_ANCHOR_DESCRIPTION("upgrade", "anchor.description"),
    //Transmission types
    TRANSMISSION_TYPE_ENERGY("transmission.mekanism.energy"),
    TRANSMISSION_TYPE_FLUID("transmission.mekanism.fluids"),
    TRANSMISSION_TYPE_GAS("transmission.mekanism.gases"),
    TRANSMISSION_TYPE_ITEM("transmission.mekanism.items"),
    TRANSMISSION_TYPE_HEAT("transmission.mekanism.heat");

    private final String key;

    APILang(String type, String path) {
        this(Util.makeTranslationKey(type, new ResourceLocation(MekanismAPI.MEKANISM_MODID, path)));
    }

    APILang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}