package mekanism.common.tile.component.config;

import java.util.Locale;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;

//TODO: Re-evaluate how we do colors, given for say energy as the transmission type it makes more sense to have input be green?
public enum DataType implements IHasTranslationKey {
    NONE("None", EnumColor.GRAY),
    INPUT("Input", EnumColor.DARK_RED),
    OUTPUT("Output", EnumColor.DARK_BLUE),
    ENERGY("Energy", EnumColor.DARK_GREEN),
    EXTRA("Extra", EnumColor.PURPLE);

    private final String name;
    private final EnumColor color;
    private final String translationKey;

    DataType(String name, EnumColor color) {
        this.name = name;
        this.color = color;
        translationKey = "side_data.mekanism." + name.toLowerCase(Locale.ROOT);
    }

    public EnumColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }
}