package mekanism.common.content.transporter;

import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

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

    public TransitRequest mapInventory(BlockEntity tile, Direction side, boolean singleItem) {
        if (sizeMode && !singleItem) {
            return TransitRequest.definedItem(tile, side, min, max, getFinder());
        }
        return TransitRequest.definedItem(tile, side, singleItem ? 1 : 64, getFinder());
    }

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.ALLOW_DEFAULT, allowDefault);
        nbtTags.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(color));
        nbtTags.putBoolean(NBTConstants.SIZE_MODE, sizeMode);
        nbtTags.putInt(NBTConstants.MIN, min);
        nbtTags.putInt(NBTConstants.MAX, max);
        return nbtTags;
    }

    @Override
    public void read(CompoundTag nbtTags) {
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.ALLOW_DEFAULT, value -> allowDefault = value);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, TransporterUtils::readColor, color -> this.color = color);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.SIZE_MODE, value -> sizeMode = value);
        NBTUtils.setIntIfPresent(nbtTags, NBTConstants.MIN, value -> min = value);
        NBTUtils.setIntIfPresent(nbtTags, NBTConstants.MAX, value -> max = value);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeBoolean(allowDefault);
        buffer.writeVarInt(TransporterUtils.getColorIndex(color));
        buffer.writeBoolean(sizeMode);
        buffer.writeVarInt(min);
        buffer.writeVarInt(max);
    }

    @Override
    public void read(FriendlyByteBuf dataStream) {
        allowDefault = dataStream.readBoolean();
        color = TransporterUtils.readColor(dataStream.readVarInt());
        sizeMode = dataStream.readBoolean();
        min = dataStream.readVarInt();
        max = dataStream.readVarInt();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + (color == null ? -1 : color.ordinal());
        code = 31 * code + (sizeMode ? 1 : 0);
        code = 31 * code + min;
        code = 31 * code + max;
        return code;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SorterFilter<?> filter && filter.color == color && filter.sizeMode == sizeMode && filter.min == min && filter.max == max;
    }
}