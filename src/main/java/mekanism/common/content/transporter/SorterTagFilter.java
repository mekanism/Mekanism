package mekanism.common.content.transporter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.lib.inventory.Finder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SorterTagFilter extends SorterFilter<SorterTagFilter> implements ITagFilter<SorterTagFilter> {

    public static final MapCodec<SorterTagFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseSorterCodec(instance)
          .and(Codec.STRING.fieldOf(SerializationConstants.TAG).forGetter(SorterTagFilter::getTagName))
          .apply(instance, SorterTagFilter::new));
    public static final StreamCodec<ByteBuf, SorterTagFilter> STREAM_CODEC = StreamCodec.composite(
          baseSorterStreamCodec(SorterTagFilter::new), Function.identity(),
          ByteBufCodecs.STRING_UTF8, SorterTagFilter::getTagName,
          (filter, tagName) -> {
              filter.tagName = tagName;
              return filter;
          }
    );

    private String tagName;

    public SorterTagFilter() {
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected SorterTagFilter(boolean enabled, boolean allowDefault, boolean sizeMode, int min, int max, Optional<EnumColor> color, String tagName) {
        super(enabled, allowDefault, sizeMode, min, max, color.orElse(null));
        this.tagName = tagName;
    }

    public SorterTagFilter(SorterTagFilter filter) {
        super(filter);
        tagName = filter.tagName;
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
        SorterTagFilter other = (SorterTagFilter) o;
        return tagName.equals(other.tagName);
    }

    @Override
    public SorterTagFilter clone() {
        return new SorterTagFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SORTER_TAG_FILTER;
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