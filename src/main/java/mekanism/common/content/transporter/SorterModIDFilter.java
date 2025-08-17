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
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.lib.inventory.Finder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SorterModIDFilter extends SorterFilter<SorterModIDFilter> implements IModIDFilter<SorterModIDFilter> {

    public static final MapCodec<SorterModIDFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseSorterCodec(instance)
          .and(Codec.STRING.fieldOf(SerializationConstants.MODID).forGetter(SorterModIDFilter::getModID))
          .apply(instance, SorterModIDFilter::new));
    public static final StreamCodec<ByteBuf, SorterModIDFilter> STREAM_CODEC = StreamCodec.composite(
          baseSorterStreamCodec(SorterModIDFilter::new), Function.identity(),
          ByteBufCodecs.STRING_UTF8, SorterModIDFilter::getModID,
          (filter, modID) -> {
              filter.modID = modID;
              return filter;
          }
    );

    private String modID;

    public SorterModIDFilter() {
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected SorterModIDFilter(boolean enabled, boolean allowDefault, boolean sizeMode, int min, int max, Optional<EnumColor> color, String modID) {
        super(enabled, allowDefault, sizeMode, min, max, color.orElse(null));
        this.modID = modID;
    }

    public SorterModIDFilter(SorterModIDFilter filter) {
        super(filter);
        modID = filter.modID;
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
        SorterModIDFilter other = (SorterModIDFilter) o;
        return modID.equals(other.modID);
    }

    @Override
    public SorterModIDFilter clone() {
        return new SorterModIDFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SORTER_MODID_FILTER;
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