package mekanism.common.content.miner;

import javax.annotation.Nonnull;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class MItemStackFilter extends MinerFilter<MItemStackFilter> implements IItemStackFilter<MItemStackFilter> {

    private ItemStack itemType = ItemStack.EMPTY;

    public MItemStackFilter(ItemStack item) {
        itemType = item;
    }

    public MItemStackFilter() {
    }

    @Override
    public boolean canFilter(BlockState state) {
        ItemStack itemStack = new ItemStack(state.getBlock());
        if (itemStack.isEmpty()) {
            return false;
        }
        return itemType.isItemEqual(itemStack);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        itemType.write(nbtTags);
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        itemType = ItemStack.read(nbtTags);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeItemStack(itemType);
    }

    @Override
    public void read(PacketBuffer dataStream) {
        super.read(dataStream);
        itemType = dataStream.readItemStack();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + itemType.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return filter instanceof MItemStackFilter && ((MItemStackFilter) filter).itemType.isItemEqual(itemType);
    }

    @Override
    public MItemStackFilter clone() {
        MItemStackFilter filter = new MItemStackFilter();
        filter.replaceStack = replaceStack;
        filter.requireStack = requireStack;
        filter.itemType = itemType.copy();
        return filter;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.MINER_ITEMSTACK_FILTER;
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