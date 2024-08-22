package mekanism.common.tile.component.config;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

//TODO: Re-evaluate how we do colors, given for say energy as the transmission type it makes more sense to have input be green?
// Maybe we should make some way to specify a color override?
@NothingNullByDefault
public enum DataType implements IIncrementalEnum<DataType>, IHasEnumNameTranslationKey, StringRepresentable {
    NONE(MekanismLang.SIDE_DATA_NONE, EnumColor.GRAY),
    INPUT(MekanismLang.SIDE_DATA_INPUT, EnumColor.DARK_RED),
    INPUT_1(MekanismLang.SIDE_DATA_INPUT_1, EnumColor.DARK_RED),
    INPUT_2(MekanismLang.SIDE_DATA_INPUT_2, EnumColor.ORANGE),
    OUTPUT(MekanismLang.SIDE_DATA_OUTPUT, EnumColor.DARK_BLUE),
    OUTPUT_1(MekanismLang.SIDE_DATA_OUTPUT_1, EnumColor.DARK_BLUE),
    OUTPUT_2(MekanismLang.SIDE_DATA_OUTPUT_2, EnumColor.DARK_AQUA),
    INPUT_OUTPUT(MekanismLang.SIDE_DATA_INPUT_OUTPUT, EnumColor.PURPLE),
    ENERGY(MekanismLang.SIDE_DATA_ENERGY, EnumColor.DARK_GREEN),
    EXTRA(MekanismLang.SIDE_DATA_EXTRA, EnumColor.YELLOW);

    public static final Codec<DataType> CODEC = StringRepresentable.fromEnum(DataType::values);
    public static final IntFunction<DataType> BY_ID = ByIdMap.continuous(DataType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, DataType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DataType::ordinal);

    private final String serializedName;
    private final EnumColor color;
    private final ILangEntry langEntry;

    DataType(ILangEntry langEntry, EnumColor color) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
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

    @Override
    public DataType byIndex(int index) {
        return BY_ID.apply(index);
    }

    public boolean canOutput() {
        return this == OUTPUT || this == INPUT_OUTPUT || this == OUTPUT_1 || this == OUTPUT_2;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}