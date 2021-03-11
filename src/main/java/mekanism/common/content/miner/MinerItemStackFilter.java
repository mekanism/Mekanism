package mekanism.common.content.miner;

import javax.annotation.Nonnull;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class MinerItemStackFilter extends MinerFilter<MinerItemStackFilter> implements IItemStackFilter<MinerItemStackFilter> {

    private ItemStack itemType = ItemStack.EMPTY;

    public MinerItemStackFilter(ItemStack item) {
        itemType = item;
    }

    public MinerItemStackFilter() {
    }

    @Override
    public boolean canFilter(BlockState state) {
        ItemStack itemStack = new ItemStack(state.getBlock());
        if (itemStack.isEmpty()) {
            return false;
        }
        return itemType.sameItem(itemStack);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        itemType.save(nbtTags);
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        itemType = ItemStack.of(nbtTags);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeItem(itemType);
    }

    @Override
    public void read(PacketBuffer dataStream) {
        super.read(dataStream);
        itemType = dataStream.readItem();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + itemType.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return filter instanceof MinerItemStackFilter && ((MinerItemStackFilter) filter).itemType.sameItem(itemType);
    }

    @Override
    public MinerItemStackFilter clone() {
        MinerItemStackFilter filter = new MinerItemStackFilter();
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