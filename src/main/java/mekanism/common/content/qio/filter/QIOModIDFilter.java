package mekanism.common.content.qio.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.lib.inventory.Finder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class QIOModIDFilter extends QIOFilter<QIOModIDFilter> implements IModIDFilter<QIOModIDFilter> {

    public static final MapCodec<QIOModIDFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseQIOCodec(instance)
          .and(Codec.STRING.fieldOf(SerializationConstants.MODID).forGetter(QIOModIDFilter::getModID))
          .apply(instance, QIOModIDFilter::new));
    public static final StreamCodec<ByteBuf, QIOModIDFilter> STREAM_CODEC = StreamCodec.composite(
          baseQIOStreamCodec(QIOModIDFilter::new), Function.identity(),
          ByteBufCodecs.STRING_UTF8, QIOModIDFilter::getModID,
          (filter, modID) -> {
              filter.modID = modID;
              return filter;
          }
    );

    private String modID;

    public QIOModIDFilter() {
    }

    protected QIOModIDFilter(boolean enabled, String modID) {
        super(enabled);
        this.modID = modID;
    }

    public QIOModIDFilter(QIOModIDFilter filter) {
        super(filter);
        this.modID = filter.modID;
    }

    @Override
    public Finder getFinder() {
        return Finder.modID(modID);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + modID.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        QIOModIDFilter other = (QIOModIDFilter) o;
        return modID.equals(other.modID);
    }

    @Override
    public QIOModIDFilter clone() {
        return new QIOModIDFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.QIO_MODID_FILTER;
    }

    @Override
    public void setModID(String id) {
        modID = id;
    }

    @Override
    public String getModID() {
        return modID;
    }
}
