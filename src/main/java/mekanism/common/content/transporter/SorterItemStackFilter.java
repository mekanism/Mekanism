package mekanism.common.content.transporter;

import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SorterItemStackFilter extends SorterFilter<SorterItemStackFilter> implements IItemStackFilter<SorterItemStackFilter> {

    private ItemStack itemType = ItemStack.EMPTY;
    public boolean fuzzyMode;

    public SorterItemStackFilter() {
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
    public CompoundTag write(CompoundTag nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.FUZZY_MODE, fuzzyMode);
        itemType.save(nbtTags);
        return nbtTags;
    }

    @Override
    public void read(CompoundTag nbtTags) {
        super.read(nbtTags);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.FUZZY_MODE, fuzzy -> fuzzyMode = fuzzy);
        itemType = ItemStack.of(nbtTags);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeBoolean(fuzzyMode);
        buffer.writeItem(itemType);
    }

    @Override
    public void read(FriendlyByteBuf dataStream) {
        super.read(dataStream);
        fuzzyMode = dataStream.readBoolean();
        itemType = dataStream.readItem();
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        code = 31 * code + itemType.hashCode();
        code = 31 * code + (fuzzyMode ? 1 : 0);
        return code;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && o instanceof SorterItemStackFilter filter && filter.itemType.sameItem(itemType) && filter.fuzzyMode == fuzzyMode;
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