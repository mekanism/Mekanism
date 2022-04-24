package mekanism.common.content.transporter;

import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class SorterModIDFilter extends SorterFilter<SorterModIDFilter> implements IModIDFilter<SorterModIDFilter> {

    private String modID;

    public SorterModIDFilter() {
    }

    public SorterModIDFilter(SorterModIDFilter filter) {
        super(filter);
        modID = filter.modID;
    }

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
        modID = BasePacketHandler.readString(dataStream);
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        code = 31 * code + modID.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && o instanceof SorterModIDFilter filter && filter.modID.equals(modID);
    }

    @Override
    public SorterModIDFilter clone() {
        return new SorterModIDFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SORTER_MODID_FILTER;
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