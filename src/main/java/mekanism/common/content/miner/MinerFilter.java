package mekanism.common.content.miner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.TileNetworkList;
import mekanism.common.content.filter.IFilter;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public abstract class MinerFilter<FILTER extends MinerFilter<FILTER>> implements IFilter<FILTER> {

    public ItemStack replaceStack = ItemStack.EMPTY;

    public boolean requireStack;

    //Mark it as abstract so it does not think clone is being implemented by Object
    @Override
    public abstract FILTER clone();

    public static MinerFilter<?> readFromNBT(CompoundNBT nbtTags) {
        MinerFilter<?> filter = getType(nbtTags.getInt(NBTConstants.TYPE));
        filter.read(nbtTags);
        return filter;
    }

    public static MinerFilter<?> readFromPacket(PacketBuffer dataStream) {
        MinerFilter<?> filter = getType(dataStream.readInt());
        filter.read(dataStream);
        return filter;
    }

    public boolean replaceStackMatches(@Nonnull ItemStack stack) {
        //TODO: Should this be ItemHandlerHelper.canItemStacksStack() instead of isItemEqual
        return !replaceStack.isEmpty() && !stack.isEmpty() && stack.isItemEqual(replaceStack);
    }

    @Nullable
    private static MinerFilter<?> getType(int type) {
        MinerFilter<?> filter = null;
        if (type == 0) {
            filter = new MItemStackFilter();
        } else if (type == 1) {
            filter = new MTagFilter();
        } else if (type == 2) {
            filter = new MMaterialFilter();
        } else if (type == 3) {
            filter = new MModIDFilter();
        }
        return filter;
    }

    public abstract boolean canFilter(BlockState state);

    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.putBoolean(NBTConstants.REQUIRE_STACK, requireStack);
        if (!replaceStack.isEmpty()) {
            nbtTags.put(NBTConstants.REPLACE_STACK, replaceStack.write(new CompoundNBT()));
        }
        return nbtTags;
    }

    protected void read(CompoundNBT nbtTags) {
        requireStack = nbtTags.getBoolean(NBTConstants.REQUIRE_STACK);
        NBTUtils.setItemStackIfPresent(nbtTags, NBTConstants.REPLACE_STACK, stack -> replaceStack = stack);
    }

    public void write(TileNetworkList data) {
        data.add(requireStack);
        data.add(replaceStack);
    }

    protected void read(PacketBuffer dataStream) {
        requireStack = dataStream.readBoolean();
        replaceStack = dataStream.readItemStack();
    }

    public abstract boolean equals(Object filter);

    public abstract int hashCode();
}