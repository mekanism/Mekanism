package mekanism.common.content.transporter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.lib.inventory.Finder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class SorterItemStackFilter extends SorterFilter<SorterItemStackFilter> implements IItemStackFilter<SorterItemStackFilter> {

    public static final MapCodec<SorterItemStackFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseSorterCodec(instance)
          .and(ItemResource.OPTIONAL_CODEC.fieldOf(SerializationConstants.TARGET_STACK).forGetter(SorterItemStackFilter::getItemType))
          .and(Codec.BOOL.optionalFieldOf(SerializationConstants.FUZZY, false).forGetter(filter -> filter.fuzzyMode))
          .apply(instance, SorterItemStackFilter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SorterItemStackFilter> STREAM_CODEC = StreamCodec.composite(
          baseSorterStreamCodec(SorterItemStackFilter::new), Function.identity(),
          ItemResource.STREAM_CODEC, SorterItemStackFilter::getItemType,
          ByteBufCodecs.BOOL, filter -> filter.fuzzyMode,
          (filter, itemType, fuzzyMode) -> {
              filter.itemType = itemType;
              filter.fuzzyMode = fuzzyMode;
              return filter;
          }
    );

    private ItemResource itemType = ItemResource.EMPTY;
    public boolean fuzzyMode;

    public SorterItemStackFilter() {
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected SorterItemStackFilter(boolean enabled, boolean allowDefault, boolean sizeMode, int min, int max, Optional<EnumColor> color, ItemResource itemType, boolean fuzzyMode) {
        super(enabled, allowDefault, sizeMode, min, max, color.orElse(null));
        this.itemType = itemType;
        this.fuzzyMode = fuzzyMode;
    }

    public SorterItemStackFilter(SorterItemStackFilter filter) {
        super(filter);
        itemType = filter.itemType;
        fuzzyMode = filter.fuzzyMode;
    }

    @Override
    public boolean test(ItemResource toCheck) {
        return fuzzyMode ? Finder.item(itemType, toCheck) : itemType.equals(toCheck);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + itemType.getItem().hashCode();
        result = 31 * result + Boolean.hashCode(fuzzyMode);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        SorterItemStackFilter other = (SorterItemStackFilter) o;
        if (fuzzyMode == other.fuzzyMode) {
            if (fuzzyMode) {
                return itemType.is(other.itemType.typeHolder());
            }
            return itemType.equals(other.itemType);
        }
        return false;
    }

    @Override
    public SorterItemStackFilter clone() {
        return new SorterItemStackFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SORTER_ITEMSTACK_FILTER;
    }

    @Override
    public ItemResource getItemType() {
        return itemType;
    }

    @Override
    public void setItemType(ItemResource itemType) {
        this.itemType = itemType;
    }
}