package mekanism.common.content.miner;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public abstract class MinerFilter<FILTER extends MinerFilter<FILTER>> extends BaseFilter<FILTER> {

    public ItemStack replaceStack = ItemStack.EMPTY;

    public boolean requireStack;

    public boolean replaceStackMatches(@Nonnull ItemStack stack) {
        //TODO: Should this be ItemHandlerHelper.canItemStacksStack() instead of isItemEqual
        // Potentially this should be be a "fuzzy" style thing as sometimes the player may want the NBT to match and other times they may not
        return !replaceStack.isEmpty() && !stack.isEmpty() && stack.isItemEqual(replaceStack);
    }

    public abstract boolean canFilter(BlockState state);

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.REQUIRE_STACK, requireStack);
        if (!replaceStack.isEmpty()) {
            nbtTags.put(NBTConstants.REPLACE_STACK, replaceStack.write(new CompoundNBT()));
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        requireStack = nbtTags.getBoolean(NBTConstants.REQUIRE_STACK);
        NBTUtils.setItemStackIfPresent(nbtTags, NBTConstants.REPLACE_STACK, stack -> replaceStack = stack);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeBoolean(requireStack);
        buffer.writeItemStack(replaceStack);
    }

    @Override
    public void read(PacketBuffer dataStream) {
        requireStack = dataStream.readBoolean();
        replaceStack = dataStream.readItemStack();
    }

    public abstract boolean equals(Object filter);

    public abstract int hashCode();
}