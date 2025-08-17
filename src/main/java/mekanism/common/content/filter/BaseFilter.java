package mekanism.common.content.filter;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import mekanism.api.SerializationConstants;
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

public abstract class BaseFilter<FILTER extends BaseFilter<FILTER>> implements IFilter<FILTER> {

    public static final Codec<IFilter<?>> GENERIC_CODEC = FilterType.CODEC.dispatch(IFilter::getFilterType, FilterType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf, IFilter<?>> GENERIC_STREAM_CODEC = FilterType.STREAM_CODEC.<RegistryFriendlyByteBuf>cast()
          .dispatch(IFilter::getFilterType, FilterType::streamCodec);

    protected static <FILTER extends BaseFilter<FILTER>> P1<Mu<FILTER>, Boolean> baseCodec(Instance<FILTER> instance) {
        return instance.group(
              Codec.BOOL.optionalFieldOf(SerializationConstants.ENABLED, true).forGetter(BaseFilter::isEnabled)
        );
    }

    protected static <FILTER extends BaseFilter<FILTER>> StreamCodec<ByteBuf, FILTER> baseStreamCodec(Supplier<FILTER> constructor) {
        return ByteBufCodecs.BOOL.map(val -> {
            FILTER filter = constructor.get();
            filter.setEnabled(val);
            return filter;
        }, BaseFilter::isEnabled);
    }

    //Enabled by default
    private boolean enabled = true;

    protected BaseFilter() {
    }

    protected BaseFilter(boolean enabled) {
        this.enabled = enabled;
    }

    protected BaseFilter(FILTER filter) {
        this(filter.isEnabled());
    }

    //Mark it as abstract, so it does not think clone is being implemented by Object
    @Override
    public abstract FILTER clone();

    @Override
    public int hashCode() {
        //Hash the filter type to ensure things like material and item filters don't collide on their hash if everything
        // else except their type is equal
        return 31 * getFilterType().hashCode() + Boolean.hashCode(enabled);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        //TODO: Eventually it might be nice to go back to having some way to not allow duplicate filters that are duplicates except for a few states
        // for example different enabled state or different allow default state for sorter filters
        BaseFilter<?> other = (BaseFilter<?>) o;
        return enabled == other.enabled;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static IFilter<?> fromType(FilterType filterType) {
        return switch (filterType) {
            case MINER_ITEMSTACK_FILTER -> new MinerItemStackFilter();
            case MINER_MODID_FILTER -> new MinerModIDFilter();
            case MINER_TAG_FILTER -> new MinerTagFilter();
            case SORTER_ITEMSTACK_FILTER -> new SorterItemStackFilter();
            case SORTER_MODID_FILTER -> new SorterModIDFilter();
            case SORTER_TAG_FILTER -> new SorterTagFilter();
            case OREDICTIONIFICATOR_ITEM_FILTER -> new OredictionificatorItemFilter();
            case QIO_ITEMSTACK_FILTER -> new QIOItemStackFilter();
            case QIO_MODID_FILTER -> new QIOModIDFilter();
            case QIO_TAG_FILTER -> new QIOTagFilter();
        };
    }
}