package mekanism.common.content.qio.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.lib.inventory.Finder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class QIOTagFilter extends QIOFilter<QIOTagFilter> implements ITagFilter<QIOTagFilter> {

    public static final MapCodec<QIOTagFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseQIOCodec(instance)
          .and(Codec.STRING.fieldOf(SerializationConstants.TAG).forGetter(QIOTagFilter::getTagName))
          .apply(instance, QIOTagFilter::new));
    public static final StreamCodec<ByteBuf, QIOTagFilter> STREAM_CODEC = StreamCodec.composite(
          baseQIOStreamCodec(QIOTagFilter::new), Function.identity(),
          ByteBufCodecs.STRING_UTF8, QIOTagFilter::getTagName,
          (filter, tagName) -> {
              filter.tagName = tagName;
              return filter;
          }
    );

    private String tagName;

    public QIOTagFilter() {
    }

    protected QIOTagFilter(boolean enabled, String tagName) {
        super(enabled);
        this.tagName = tagName;
    }

    public QIOTagFilter(QIOTagFilter filter) {
        super(filter);
        this.tagName = filter.tagName;
    }

    @Override
    public Finder getFinder() {
        return Finder.tag(tagName);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + tagName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        QIOTagFilter other = (QIOTagFilter) o;
        return tagName.equals(other.tagName);
    }

    @Override
    public QIOTagFilter clone() {
        return new QIOTagFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.QIO_TAG_FILTER;
    }

    @Override
    public void setTagName(String name) {
        tagName = name;
    }

    @Override
    public String getTagName() {
        return tagName;
    }
}
