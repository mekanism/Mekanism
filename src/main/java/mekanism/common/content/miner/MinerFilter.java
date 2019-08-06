package mekanism.common.content.miner;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.common.content.filter.IFilter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public abstract class MinerFilter implements IFilter {

    public ItemStack replaceStack = ItemStack.EMPTY;

    public boolean requireStack;

    public static MinerFilter readFromNBT(CompoundNBT nbtTags) {
        MinerFilter filter = getType(nbtTags.getInteger("type"));
        filter.read(nbtTags);
        return filter;
    }

    public static MinerFilter readFromPacket(ByteBuf dataStream) {
        MinerFilter filter = getType(dataStream.readInt());
        filter.read(dataStream);
        return filter;
    }

    @Nullable
    private static MinerFilter getType(int type) {
        MinerFilter filter = null;
        if (type == 0) {
            filter = new MItemStackFilter();
        } else if (type == 1) {
            filter = new MOreDictFilter();
        } else if (type == 2) {
            filter = new MMaterialFilter();
        } else if (type == 3) {
            filter = new MModIDFilter();
        }
        return filter;
    }

    public abstract boolean canFilter(ItemStack itemStack);

    public CompoundNBT write(CompoundNBT nbtTags) {
        nbtTags.setBoolean("requireStack", requireStack);
        if (!replaceStack.isEmpty()) {
            nbtTags.setTag("replaceStack", replaceStack.writeToNBT(new CompoundNBT()));
        }
        return nbtTags;
    }

    protected void read(CompoundNBT nbtTags) {
        requireStack = nbtTags.getBoolean("requireStack");
        if (nbtTags.hasKey("replaceStack")) {
            replaceStack = new ItemStack(nbtTags.getCompoundTag("replaceStack"));
        }
    }

    public void write(TileNetworkList data) {
        data.add(requireStack);
        if (!replaceStack.isEmpty()) {
            data.add(true);
            data.add(MekanismUtils.getID(replaceStack));
            data.add(replaceStack.getItemDamage());
        } else {
            data.add(false);
        }
    }

    protected void read(ByteBuf dataStream) {
        requireStack = dataStream.readBoolean();
        if (dataStream.readBoolean()) {
            replaceStack = new ItemStack(Item.getItemById(dataStream.readInt()), 1, dataStream.readInt());
        } else {
            replaceStack = ItemStack.EMPTY;
        }
    }

    @Override
    public boolean equals(Object filter) {
        return filter instanceof MinerFilter;
    }
}