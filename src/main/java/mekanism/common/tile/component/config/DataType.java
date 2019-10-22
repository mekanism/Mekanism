package mekanism.common.tile.component.config;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;

//TODO: Re-evaluate how we do colors, given for say energy as the transmission type it makes more sense to have input be green?
public enum DataType implements IIncrementalEnum<DataType>, IHasTranslationKey {
    NONE("None", EnumColor.GRAY),
    INPUT("Input", EnumColor.DARK_RED),
    OUTPUT("Output", EnumColor.DARK_BLUE),
    ENERGY("Energy", EnumColor.DARK_GREEN),
    EXTRA("Extra", EnumColor.PURPLE);

    private static final DataType[] TYPES = values();
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

    @Nonnull
    @Override
    public DataType byIndex(int index) {
        return byIndexStatic(index);
    }

    public static DataType byIndexStatic(int index) {
        //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
        return TYPES[Math.floorMod(index, TYPES.length)];
    }
}