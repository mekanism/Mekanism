package mekanism.common.content.qio.filter;

import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class QIOTagFilter extends QIOFilter<QIOTagFilter> implements ITagFilter<QIOTagFilter> {

    private String tagName;

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
        return Objects.hash(super.hashCode(), tagName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        QIOTagFilter other = (QIOTagFilter) o;
        return tagName.equals(other.tagName);
    }

    @Override
    public QIOTagFilter clone() {
        QIOTagFilter filter = new QIOTagFilter();
        filter.tagName = tagName;
        return filter;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.QIO_TAG_FILTER;
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
