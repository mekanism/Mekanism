package mekanism.common.content.miner;

import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.content.filter.IItemStackFilter;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class MItemStackFilter extends MinerFilter<MItemStackFilter> implements IItemStackFilter<MItemStackFilter> {

    private ItemStack itemType = ItemStack.EMPTY;
    //TODO: Fuzzy doesn't do anything due the the removal of meta data
    public boolean fuzzy;

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
        if (itemStack.getItem() == itemType.getItem() && fuzzy) {
            return true;
        }
        return itemType.isItemEqual(itemStack);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("type", 0);
        nbtTags.putBoolean("fuzzy", fuzzy);
        itemType.write(nbtTags);
        return nbtTags;
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        fuzzy = nbtTags.getBoolean("fuzzy");
        itemType = ItemStack.read(nbtTags);
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(0);
        super.write(data);
        data.add(fuzzy);
        data.add(itemType);
    }

    @Override
    protected void read(PacketBuffer dataStream) {
        super.read(dataStream);
        fuzzy = dataStream.readBoolean();
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
        filter.fuzzy = fuzzy;
        filter.itemType = itemType.copy();
        return filter;
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