package mekanism.common.content.transporter;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.transporter.Finder.ItemStackFinder;
import mekanism.common.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.ItemHandlerHelper;

public class TItemStackFilter extends TransporterFilter<TItemStackFilter> implements IItemStackFilter<TItemStackFilter> {

    public boolean sizeMode;
    public boolean fuzzyMode;

    public int min;
    public int max;

    private ItemStack itemType = ItemStack.EMPTY;

    @Override
    public boolean canFilter(ItemStack itemStack, boolean strict) {
        return super.canFilter(itemStack, strict) && !(strict && sizeMode && (max == 0 || itemStack.getCount() < min))
               && (fuzzyMode ? ItemStack.areItemsEqual(itemType, itemStack) : ItemHandlerHelper.canItemStacksStack(itemType, itemStack));
    }

    @Override
    public InvStack getStackFromInventory(TileEntity tile, Direction side, boolean singleItem) {
        if (sizeMode && !singleItem) {
            return InventoryUtils.takeDefinedItem(tile, side, itemType, min, max);
        }
        return super.getStackFromInventory(tile, side, singleItem);
    }

    @Override
    public Finder getFinder() {
        return new ItemStackFinder(itemType);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.SIZE_MODE, sizeMode);
        nbtTags.putBoolean(NBTConstants.FUZZY_MODE, fuzzyMode);
        nbtTags.putInt(NBTConstants.MIN, min);
        nbtTags.putInt(NBTConstants.MAX, max);
        itemType.write(nbtTags);
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        sizeMode = nbtTags.getBoolean(NBTConstants.SIZE_MODE);
        fuzzyMode = nbtTags.getBoolean(NBTConstants.FUZZY_MODE);
        min = nbtTags.getInt(NBTConstants.MIN);
        max = nbtTags.getInt(NBTConstants.MAX);
        itemType = ItemStack.read(nbtTags);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeBoolean(sizeMode);
        buffer.writeBoolean(fuzzyMode);
        buffer.writeVarInt(min);
        buffer.writeVarInt(max);
        buffer.writeItemStack(itemType);
    }

    @Override
    public void read(PacketBuffer dataStream) {
        super.read(dataStream);
        sizeMode = dataStream.readBoolean();
        fuzzyMode = dataStream.readBoolean();
        min = dataStream.readVarInt();
        max = dataStream.readVarInt();
        itemType = dataStream.readItemStack();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + super.hashCode();
        code = 31 * code + itemType.hashCode();
        code = 31 * code + (sizeMode ? 1 : 0);
        code = 31 * code + (fuzzyMode ? 1 : 0);
        code = 31 * code + min;
        code = 31 * code + max;
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof TItemStackFilter && ((TItemStackFilter) filter).itemType.isItemEqual(itemType)
               && ((TItemStackFilter) filter).sizeMode == sizeMode && ((TItemStackFilter) filter).fuzzyMode == fuzzyMode && ((TItemStackFilter) filter).min == min
               && ((TItemStackFilter) filter).max == max;
    }

    @Override
    public TItemStackFilter clone() {
        TItemStackFilter filter = new TItemStackFilter();
        filter.allowDefault = allowDefault;
        filter.color = color;
        filter.itemType = itemType.copy();
        filter.sizeMode = sizeMode;
        filter.fuzzyMode = fuzzyMode;
        filter.min = min;
        filter.max = max;
        return filter;
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