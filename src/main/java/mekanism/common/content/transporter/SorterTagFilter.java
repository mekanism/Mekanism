package mekanism.common.content.transporter;

import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class SorterTagFilter extends SorterFilter<SorterTagFilter> implements ITagFilter<SorterTagFilter> {

    private String tagName;

    public SorterTagFilter() {
    }

    public SorterTagFilter(SorterTagFilter filter) {
        super(filter);
        tagName = filter.tagName;
    }

    @Override
    public Finder getFinder() {
        return Finder.tag(tagName);
    }

    @Override
    public CompoundTag write(CompoundTag nbtTags) {
        super.write(nbtTags);
        nbtTags.putString(NBTConstants.TAG_NAME, tagName);
        return nbtTags;
    }

    @Override
    public void read(CompoundTag nbtTags) {
        super.read(nbtTags);
        tagName = nbtTags.getString(NBTConstants.TAG_NAME);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeUtf(tagName);
    }

    @Override
    public void read(FriendlyByteBuf dataStream) {
        super.read(dataStream);
        tagName = BasePacketHandler.readString(dataStream);
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        code = 31 * code + tagName.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && o instanceof SorterTagFilter filter && filter.tagName.equals(tagName);
    }

    @Override
    public SorterTagFilter clone() {
        return new SorterTagFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SORTER_TAG_FILTER;
    }

    @Override
    public void setTagName(String name) {
        tagName = name;
    }

    @Override
    public String getTagName() {
        return tagName;
    }
}