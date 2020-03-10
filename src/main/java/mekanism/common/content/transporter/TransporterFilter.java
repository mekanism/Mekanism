package mekanism.common.content.transporter;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.IFilter;
import mekanism.common.util.NBTUtils;
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
        TransporterFilter<?> filter = getType(nbtTags.getInt(NBTConstants.TYPE));
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
        nbtTags.putBoolean(NBTConstants.ALLOW_DEFAULT, allowDefault);
        nbtTags.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(color));
    }

    protected void read(CompoundNBT nbtTags) {
        allowDefault = nbtTags.getBoolean(NBTConstants.ALLOW_DEFAULT);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, TransporterUtils::readColor, color -> this.color = color);
    }

    public void write(TileNetworkList data) {
        data.add(allowDefault);
        data.add(TransporterUtils.getColorIndex(color));
    }

    protected void read(PacketBuffer dataStream) {
        allowDefault = dataStream.readBoolean();
        color = TransporterUtils.readColor(dataStream.readInt());
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