package mekanism.common.content.qio.filter;

import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class QIOModIDFilter extends QIOFilter<QIOModIDFilter> implements IModIDFilter<QIOModIDFilter> {

    private String modID;

    @Override
    public Finder getFinder() {
        return Finder.modID(modID);
    }

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        super.write(nbtTags);
        nbtTags.putString(NBTConstants.MODID, modID);
        return nbtTags;
    }

    @Override
    public void read(CompoundTag nbtTags) {
        modID = nbtTags.getString(NBTConstants.MODID);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeUtf(modID);
    }

    @Override
    public void read(FriendlyByteBuf dataStream) {
        modID = BasePacketHandler.readString(dataStream);
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + modID.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QIOModIDFilter filter && filter.modID.equals(modID);
    }

    @Override
    public QIOModIDFilter clone() {
        QIOModIDFilter filter = new QIOModIDFilter();
        filter.modID = modID;
        return filter;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.QIO_MODID_FILTER;
    }

    @Override
    public void setModID(String id) {
        modID = id;
    }

    @Override
    public String getModID() {
        return modID;
    }
}
