package mekanism.common.content.miner;

import java.util.Objects;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.tags.MekanismTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MinerItemStackFilter extends MinerFilter<MinerItemStackFilter> implements IItemStackFilter<MinerItemStackFilter> {

    private ItemStack itemType = ItemStack.EMPTY;

    public MinerItemStackFilter(ItemStack item) {
        itemType = item;
    }

    public MinerItemStackFilter() {
    }

    public MinerItemStackFilter(MinerItemStackFilter filter) {
        super(filter);
        itemType = filter.itemType.copy();
    }

    @Override
    public boolean canFilter(BlockState state) {
        ItemStack itemStack = new ItemStack(state.getBlock());
        if (itemStack.isEmpty()) {
            return false;
        }
        return itemType.getItem() == itemStack.getItem();
    }

    @Override
    public boolean hasBlacklistedElement() {
        return !itemType.isEmpty() && itemType.getItem() instanceof BlockItem blockItem && MekanismTags.Blocks.MINER_BLACKLIST_LOOKUP.contains(blockItem.getBlock());
    }

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        super.write(nbtTags);
        itemType.save(nbtTags);
        return nbtTags;
    }

    @Override
    public void read(CompoundTag nbtTags) {
        super.read(nbtTags);
        itemType = ItemStack.of(nbtTags);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeItem(itemType);
    }

    @Override
    public void read(FriendlyByteBuf dataStream) {
        super.read(dataStream);
        itemType = dataStream.readItem();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), itemType.getItem());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        return itemType.getItem() == ((MinerItemStackFilter) o).itemType.getItem();
    }

    @Override
    public MinerItemStackFilter clone() {
        return new MinerItemStackFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.MINER_ITEMSTACK_FILTER;
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