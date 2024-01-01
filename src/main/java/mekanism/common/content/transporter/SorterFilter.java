package mekanism.common.content.transporter;

import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class SorterFilter<FILTER extends SorterFilter<FILTER>> extends BaseFilter<FILTER> {

    public static final int MAX_LENGTH = 48;

    @SyntheticComputerMethod(getter = "getColor", setter = "setColor", threadSafeGetter = true, threadSafeSetter = true)
    public EnumColor color;
    @SyntheticComputerMethod(getter = "getAllowDefault", setter = "setAllowDefault", threadSafeGetter = true, threadSafeSetter = true)
    public boolean allowDefault;
    @SyntheticComputerMethod(getter = "getSizeMode", setter = "setSizeMode", threadSafeSetter = true, threadSafeGetter = true)
    public boolean sizeMode;
    @SyntheticComputerMethod(getter = "getMin", threadSafeGetter = true)
    public int min;
    @SyntheticComputerMethod(getter = "getMax", threadSafeGetter = true)
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

    public TransitRequest mapInventory(IItemHandler itemHandler, boolean singleItem) {
        if (sizeMode && !singleItem) {
            return TransitRequest.definedItem(itemHandler, min, max, getFinder());
        }
        return TransitRequest.definedItem(itemHandler, singleItem ? 1 : 64, getFinder());
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
        super.read(nbtTags);
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
        super.read(dataStream);
        allowDefault = dataStream.readBoolean();
        color = TransporterUtils.readColor(dataStream.readVarInt());
        sizeMode = dataStream.readBoolean();
        min = dataStream.readVarInt();
        max = dataStream.readVarInt();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), color, allowDefault, sizeMode, min, max);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        SorterFilter<?> other = (SorterFilter<?>) o;
        return allowDefault == other.allowDefault && sizeMode == other.sizeMode && min == other.min && max == other.max && color == other.color;
    }

    @ComputerMethod(threadSafe = true)
    void setMinMax(int min, int max) throws ComputerException {
        if (min < 0 || max < 0 || min > max || max > 64) {
            throw new ComputerException("Invalid or min/max: 0 <= min <= max <= 64");
        }
        this.min = min;
        this.max = max;
    }

    @Override
    @ComputerMethod(threadSafe = true)
    public abstract FILTER clone();
}