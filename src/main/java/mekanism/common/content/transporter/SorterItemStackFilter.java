package mekanism.common.content.transporter;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

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
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.FUZZY_MODE, fuzzyMode);
        itemType.save(nbtTags);
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.FUZZY_MODE, fuzzy -> fuzzyMode = fuzzy);
        itemType = ItemStack.of(nbtTags);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeBoolean(fuzzyMode);
        buffer.writeItem(itemType);
    }

    @Override
    public void read(PacketBuffer dataStream) {
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
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof SorterItemStackFilter && ((SorterItemStackFilter) filter).itemType.sameItem(itemType)
               && ((SorterItemStackFilter) filter).fuzzyMode == fuzzyMode;
    }

    @Override
    public SorterItemStackFilter clone() {
        return new SorterItemStackFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SORTER_ITEMSTACK_FILTER;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        return itemType;
    }

    @Override
    public void setItemStack(@Nonnull ItemStack stack) {
        itemType = stack;
    }
}