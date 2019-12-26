package mekanism.common.tile.component.config;

import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;

//TODO: Re-evaluate how we do colors, given for say energy as the transmission type it makes more sense to have input be green?
public enum DataType implements IIncrementalEnum<DataType>, IHasTranslationKey {
    NONE(MekanismLang.SIDE_DATA_NONE, EnumColor.GRAY),
    INPUT(MekanismLang.SIDE_DATA_INPUT, EnumColor.DARK_RED),
    OUTPUT(MekanismLang.SIDE_DATA_OUTPUT, EnumColor.DARK_BLUE),
    ENERGY(MekanismLang.SIDE_DATA_ENERGY, EnumColor.DARK_GREEN),
    EXTRA(MekanismLang.SIDE_DATA_EXTRA, EnumColor.PURPLE);

    private static final DataType[] TYPES = values();
    private final EnumColor color;
    private final ILangEntry langEntry;

    DataType(ILangEntry langEntry, EnumColor color) {
        this.color = color;
        this.langEntry = langEntry;
    }

    public EnumColor getColor() {
        return color;
    }

    @Override
    public String getTranslationKey() {
        return langEntry.getTranslationKey();
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