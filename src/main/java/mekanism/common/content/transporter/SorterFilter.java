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

    public abstract Finder getFinder();

    public TransitRequest mapInventory(TileEntity tile, Direction side, boolean singleItem) {
        return TransitRequest.definedItem(tile, side, singleItem ? 1 : 64, getFinder());
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.ALLOW_DEFAULT, allowDefault);
        nbtTags.putInt(NBTConstants.COLOR, TransporterUtils.getColorIndex(color));
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        allowDefault = nbtTags.getBoolean(NBTConstants.ALLOW_DEFAULT);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.COLOR, TransporterUtils::readColor, color -> this.color = color);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeBoolean(allowDefault);
        buffer.writeVarInt(TransporterUtils.getColorIndex(color));
    }

    @Override
    public void read(PacketBuffer dataStream) {
        allowDefault = dataStream.readBoolean();
        color = TransporterUtils.readColor(dataStream.readVarInt());
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + (color != null ? color.ordinal() : -1);
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return filter instanceof SorterFilter && ((SorterFilter<?>) filter).color == color;
    }
}