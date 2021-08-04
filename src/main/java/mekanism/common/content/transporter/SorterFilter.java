package mekanism.common.content.transporter;

import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public abstract class SorterFilter<FILTER extends SorterFilter<FILTER>> extends BaseFilter<FILTER> {

    public static final int MAX_LENGTH = 48;

    public EnumColor color;
    public boolean allowDefault;
    public boolean sizeMode;
    public int min;
    public int max;

    protected SorterFilter() {
    }

    protected SorterFilter(FILTER filter) {
        allowDefault = filter.allowDefault;
        color = filter.color;
        sizeMode = filter.sizeMode;
        min = filter.min;
        max = filter.max;
    }

    public abstract Finder getFinder();

    public TransitRequest mapInventory(TileEntity tile, Direction side, boolean singleItem) {
        if (sizeMode && !singleItem) {
            return TransitRequest.definedItem(tile, side, min, max, getFinder());
        }
        return TransitRequest.definedItem(tile, side, singleItem ? 1 : 64, getFinder());
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.ALLOW_DEFAULT, allowDefault);
        nbtTags.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(color));
        nbtTags.putBoolean(NBTConstants.SIZE_MODE, sizeMode);
        nbtTags.putInt(NBTConstants.MIN, min);
        nbtTags.putInt(NBTConstants.MAX, max);
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.ALLOW_DEFAULT, value -> allowDefault = value);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, TransporterUtils::readColor, color -> this.color = color);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.SIZE_MODE, value -> sizeMode = value);
        NBTUtils.setIntIfPresent(nbtTags, NBTConstants.MIN, value -> min = value);
        NBTUtils.setIntIfPresent(nbtTags, NBTConstants.MAX, value -> max = value);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeBoolean(allowDefault);
        buffer.writeVarInt(TransporterUtils.getColorIndex(color));
        buffer.writeBoolean(sizeMode);
        buffer.writeVarInt(min);
        buffer.writeVarInt(max);
    }

    @Override
    public void read(PacketBuffer dataStream) {
        allowDefault = dataStream.readBoolean();
        color = TransporterUtils.readColor(dataStream.readVarInt());
        sizeMode = dataStream.readBoolean();
        min = dataStream.readVarInt();
        max = dataStream.readVarInt();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + (color != null ? color.ordinal() : -1);
        code = 31 * code + (sizeMode ? 1 : 0);
        code = 31 * code + min;
        code = 31 * code + max;
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return filter instanceof SorterFilter && ((SorterFilter<?>) filter).color == color && ((SorterFilter<?>) filter).sizeMode == sizeMode
               && ((SorterFilter<?>) filter).min == min && ((SorterFilter<?>) filter).max == max;
    }
}