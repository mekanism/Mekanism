package mekanism.common.content.transporter;

import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

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
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putString(NBTConstants.MODID, modID);
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        modID = nbtTags.getString(NBTConstants.MODID);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeUtf(modID);
    }

    @Override
    public void read(PacketBuffer dataStream) {
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
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof SorterModIDFilter && ((SorterModIDFilter) filter).modID.equals(modID);
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