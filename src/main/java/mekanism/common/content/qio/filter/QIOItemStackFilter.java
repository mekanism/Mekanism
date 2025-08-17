package mekanism.common.content.qio.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.lib.inventory.Finder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class QIOItemStackFilter extends QIOFilter<QIOItemStackFilter> implements IItemStackFilter<QIOItemStackFilter> {

    public static final MapCodec<QIOItemStackFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseQIOCodec(instance)
          .and(ItemStack.SINGLE_ITEM_CODEC.fieldOf(SerializationConstants.TARGET_STACK).forGetter(QIOItemStackFilter::getItemStack))
          .and(Codec.BOOL.optionalFieldOf(SerializationConstants.FUZZY, false).forGetter(filter -> filter.fuzzyMode))
          .apply(instance, QIOItemStackFilter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, QIOItemStackFilter> STREAM_CODEC = StreamCodec.composite(
          baseQIOStreamCodec(QIOItemStackFilter::new), Function.identity(),
          ItemStack.OPTIONAL_STREAM_CODEC, QIOItemStackFilter::getItemStack,
          ByteBufCodecs.BOOL, filter -> filter.fuzzyMode,
          (filter, itemType, fuzzyMode) -> {
              filter.itemType = itemType;
              filter.fuzzyMode = fuzzyMode;
              return filter;
          }
    );

    private ItemStack itemType = ItemStack.EMPTY;
    public boolean fuzzyMode;

    public QIOItemStackFilter() {
    }

    protected QIOItemStackFilter(boolean enabled, ItemStack itemType, boolean fuzzyMode) {
        super(enabled);
        this.itemType = itemType;
        this.fuzzyMode = fuzzyMode;
    }

    public QIOItemStackFilter(QIOItemStackFilter filter) {
        super(filter);
        this.itemType = filter.itemType.copy();
        this.fuzzyMode = filter.fuzzyMode;
    }

    @Override
    public Finder getFinder() {
        return fuzzyMode ? Finder.item(itemType) : Finder.strict(itemType);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + itemType.getItem().hashCode();
        result = 31 * result + Boolean.hashCode(fuzzyMode);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        QIOItemStackFilter other = (QIOItemStackFilter) o;
        if (fuzzyMode == other.fuzzyMode) {
            if (fuzzyMode) {
                return itemType.getItem() == other.itemType.getItem();
            }
            return ItemStack.isSameItemSameComponents(itemType, other.itemType);
        }
        return false;
    }

    @Override
    public QIOItemStackFilter clone() {
        return new QIOItemStackFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.QIO_ITEMSTACK_FILTER;
    }

    @NotNull
    @Override
    public ItemStack getItemStack() {
        return itemType;
    }

    @Override
    public void setItemStack(@NotNull ItemStack stack) {
        itemType = stack;
    }
}
