package mekanism.api.text;

import mekanism.api.MekanismAPI;
import net.minecraft.resources.Identifier;

/// Lang entries declared in the API and provided by Mekanism.
///
/// @apiNote These should only be accessed via their corresponding users, except for use in making it easier to not miss any entries in the DataGenerators
public enum APILang implements ILangEntry {
    //Boolean
    TRUE_LOWER("gui", "true_lower"),
    FALSE_LOWER("gui", "false_lower"),
    //Directions
    DOWN("direction", "down"),
    UP("direction", "up"),
    NORTH("direction", "north"),
    SOUTH("direction", "south"),
    WEST("direction", "west"),
    EAST("direction", "east"),
    //Relative Sides
    FRONT("side", "front"),
    LEFT("side", "left"),
    RIGHT("side", "right"),
    BACK("side", "back"),
    TOP("side", "top"),
    BOTTOM("side", "bottom"),
    //Chemical Attributes
    CHEMICAL_ATTRIBUTE_RADIATION("chemical", "attribute.radiation"),
    CHEMICAL_ATTRIBUTE_COOLANT_EFFICIENCY("chemical", "attribute.coolant.efficiency"),
    CHEMICAL_ATTRIBUTE_COOLANT_ENTHALPY("chemical", "attribute.coolant.heat_capacity"),
    CHEMICAL_ATTRIBUTE_COOLANT_TEMPERATURE("chemical", "attribute.coolant.temperature"),
    CHEMICAL_ATTRIBUTE_FUEL_MAX_BURN("chemical", "attribute.fuel.max_burn"),
    CHEMICAL_ATTRIBUTE_FUEL_ENERGY_DENSITY("chemical", "attribute.fuel.energy_density"),
    CHEMICAL_ATTRIBUTE_FUEL_ENERGY_MAX_TOTAL("chemical", "attribute.fuel.energy_max_total"),
    //Security
    PUBLIC("security", "public"),
    TRUSTED("security", "trusted"),
    PRIVATE("security", "private"),
    //Tooltip
    DECAY_IMMUNE("tooltip", "decay_immune"),
    ;

    private final String key;

    APILang(String type, String path) {
        this(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, path).toLanguageKey(type));
    }

    APILang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}