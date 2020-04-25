package mekanism.common.tile.component.config;

import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;

//TODO: Re-evaluate how we do colors, given for say energy as the transmission type it makes more sense to have input be green?
// Maybe we should make some way to specify a color override?
public enum DataType implements IIncrementalEnum<DataType>, IHasTranslationKey {
    NONE(MekanismLang.SIDE_DATA_NONE, EnumColor.GRAY),
    INPUT(MekanismLang.SIDE_DATA_INPUT, EnumColor.DARK_RED),
    INPUT_1(MekanismLang.SIDE_DATA_INPUT_1, EnumColor.DARK_RED),
    INPUT_2(MekanismLang.SIDE_DATA_INPUT_2, EnumColor.ORANGE),
    OUTPUT(MekanismLang.SIDE_DATA_OUTPUT, EnumColor.DARK_BLUE),
    OUTPUT_1(MekanismLang.SIDE_DATA_OUTPUT_1, EnumColor.DARK_BLUE),
    OUTPUT_2(MekanismLang.SIDE_DATA_OUTPUT_2, EnumColor.DARK_AQUA),
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
        return MathUtils.getByIndexMod(TYPES, index);
    }
}