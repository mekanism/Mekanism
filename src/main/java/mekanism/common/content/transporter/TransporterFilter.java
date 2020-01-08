package mekanism.common.content.transporter;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.IFilter;
import mekanism.common.util.TransporterUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public abstract class TransporterFilter<FILTER extends TransporterFilter<FILTER>> implements IFilter<FILTER> {

    public static final int MAX_LENGTH = 48;

    public static final List<Character> SPECIAL_CHARS = Arrays.asList('*', '-', ' ', '|', '_', '\'');

    public EnumColor color;

    public boolean allowDefault;

    //Mark it as abstract so it does not think clone is being implemented by Object
    @Override
    public abstract FILTER clone();

    public static TransporterFilter<?> readFromNBT(CompoundNBT nbtTags) {
        TransporterFilter<?> filter = getType(nbtTags.getInt("type"));
        filter.read(nbtTags);
        return filter;
    }

    public static TransporterFilter<?> readFromPacket(PacketBuffer dataStream) {
        TransporterFilter<?> filter = getType(dataStream.readInt());
        filter.read(dataStream);
        return filter;
    }

    @Nullable
    private static TransporterFilter<?> getType(int type) {
        TransporterFilter<?> filter = null;
        if (type == 0) {
            filter = new TItemStackFilter();
        } else if (type == 1) {
            filter = new TTagFilter();
        } else if (type == 2) {
            filter = new TMaterialFilter();
        } else if (type == 3) {
            filter = new TModIDFilter();
        }
        return filter;
    }

    public boolean canFilter(ItemStack itemStack, boolean strict) {
        return !itemStack.isEmpty();
    }

    public abstract Finder getFinder();

    public InvStack getStackFromInventory(StackSearcher searcher, boolean singleItem) {
        return searcher.takeTopStack(getFinder(), singleItem ? 1 : 64);
    }

    public void write(CompoundNBT nbtTags) {
        nbtTags.putBoolean("allowDefault", allowDefault);
        if (color != null) {
            nbtTags.putInt("color", TransporterUtils.colors.indexOf(color));
        }
    }

    protected void read(CompoundNBT nbtTags) {
        allowDefault = nbtTags.getBoolean("allowDefault");
        if (nbtTags.contains("color")) {
            color = TransporterUtils.colors.get(nbtTags.getInt("color"));
        }
    }

    public void write(TileNetworkList data) {
        data.add(allowDefault);
        if (color != null) {
            data.add(TransporterUtils.colors.indexOf(color));
        } else {
            data.add(-1);
        }
    }

    protected void read(PacketBuffer dataStream) {
        allowDefault = dataStream.readBoolean();
        int c = dataStream.readInt();
        if (c != -1) {
            color = TransporterUtils.colors.get(c);
        } else {
            color = null;
        }
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + (color != null ? color.ordinal() : -1);
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return filter instanceof TransporterFilter && ((TransporterFilter<?>) filter).color == color;
    }
}