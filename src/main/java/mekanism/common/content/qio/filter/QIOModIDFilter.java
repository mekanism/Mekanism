package mekanism.common.content.qio.filter;

import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.lib.inventory.Finder;
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
        super.read(nbtTags);
        modID = nbtTags.getString(NBTConstants.MODID);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeUtf(modID);
    }

    @Override
    public void read(FriendlyByteBuf dataStream) {
        super.read(dataStream);
        modID = dataStream.readUtf();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), modID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        QIOModIDFilter other = (QIOModIDFilter) o;
        return modID.equals(other.modID);
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
