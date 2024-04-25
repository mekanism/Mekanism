package mekanism.common.content.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum FilterType implements StringRepresentable {
    MINER_ITEMSTACK_FILTER(MinerItemStackFilter.CODEC, MinerItemStackFilter.STREAM_CODEC),
    MINER_MODID_FILTER(MinerModIDFilter.CODEC, MinerModIDFilter.STREAM_CODEC),
    MINER_TAG_FILTER(MinerTagFilter.CODEC, MinerTagFilter.STREAM_CODEC),
    SORTER_ITEMSTACK_FILTER(SorterItemStackFilter.CODEC, SorterItemStackFilter.STREAM_CODEC),
    SORTER_MODID_FILTER(SorterModIDFilter.CODEC, SorterModIDFilter.STREAM_CODEC),
    SORTER_TAG_FILTER(SorterTagFilter.CODEC, SorterTagFilter.STREAM_CODEC),
    OREDICTIONIFICATOR_ITEM_FILTER(OredictionificatorItemFilter.CODEC, OredictionificatorItemFilter.STREAM_CODEC),
    QIO_ITEMSTACK_FILTER(QIOItemStackFilter.CODEC, QIOItemStackFilter.STREAM_CODEC),
    QIO_MODID_FILTER(QIOModIDFilter.CODEC, QIOModIDFilter.STREAM_CODEC),
    QIO_TAG_FILTER(QIOTagFilter.CODEC, QIOTagFilter.STREAM_CODEC);

    public static final Codec<FilterType> CODEC = StringRepresentable.fromEnum(FilterType::values);
    public static final IntFunction<FilterType> BY_ID = ByIdMap.continuous(FilterType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, FilterType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FilterType::ordinal);

    private final StreamCodec<? super RegistryFriendlyByteBuf, ? extends IFilter<?>> streamCodec;
    private final MapCodec<? extends IFilter<?>> codec;
    private final String serializedName;

    <FILTER extends IFilter<FILTER>> FilterType(MapCodec<FILTER> codec, StreamCodec<? super RegistryFriendlyByteBuf, FILTER> streamCodec) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        this.codec = codec;
        this.streamCodec = streamCodec;
    }

    public MapCodec<? extends IFilter<?>> codec() {
        return codec;
    }

    public StreamCodec<? super RegistryFriendlyByteBuf, ? extends IFilter<?>> streamCodec() {
        return streamCodec;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return serializedName;
    }
}