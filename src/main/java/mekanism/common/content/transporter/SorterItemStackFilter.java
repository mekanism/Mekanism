package mekanism.common.content.transporter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SorterItemStackFilter extends SorterFilter<SorterItemStackFilter> implements IItemStackFilter<SorterItemStackFilter> {

    public static final MapCodec<SorterItemStackFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseSorterCodec(instance)
          .and(ItemStack.OPTIONAL_CODEC.fieldOf(NBTConstants.TARGET_STACK).forGetter(SorterItemStackFilter::getItemStack))
          .and(Codec.BOOL.optionalFieldOf(NBTConstants.FUZZY_MODE, false).forGetter(filter -> filter.fuzzyMode))
          .apply(instance, SorterItemStackFilter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SorterItemStackFilter> STREAM_CODEC = StreamCodec.composite(
          baseSorterStreamCodec(SorterItemStackFilter::new), Function.identity(),
          ItemStack.OPTIONAL_STREAM_CODEC, SorterItemStackFilter::getItemStack,
          ByteBufCodecs.BOOL, filter -> filter.fuzzyMode,
          (filter, itemType, fuzzyMode) -> {
              filter.itemType = itemType;
              filter.fuzzyMode = fuzzyMode;
              return filter;
          }
    );

    private ItemStack itemType = ItemStack.EMPTY;
    public boolean fuzzyMode;

    public SorterItemStackFilter() {
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected SorterItemStackFilter(boolean enabled, boolean allowDefault, boolean sizeMode, int min, int max, Optional<EnumColor> color, ItemStack itemType, boolean fuzzyMode) {
        super(enabled, allowDefault, sizeMode, min, max, color.orElse(null));
        this.itemType = itemType;
        this.fuzzyMode = fuzzyMode;
    }

    public SorterItemStackFilter(SorterItemStackFilter filter) {
        super(filter);
        itemType = filter.itemType.copy();
        fuzzyMode = filter.fuzzyMode;
    }

    @Override
    public Finder getFinder() {
        return fuzzyMode ? Finder.item(itemType) : Finder.strict(itemType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), itemType.getItem(), fuzzyMode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        SorterItemStackFilter other = (SorterItemStackFilter) o;
        if (fuzzyMode == other.fuzzyMode) {
            if (fuzzyMode) {
                return itemType.getItem() == other.itemType.getItem();
            }
            return ItemStack.isSameItemSameComponents(itemType, other.itemType);
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