package mekanism.common.content.transporter;

import mekanism.api.TileNetworkList;
import mekanism.common.PacketHandler;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.transporter.Finder.TagFinder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class TTagFilter extends TransporterFilter<TTagFilter> implements ITagFilter<TTagFilter> {

    private String tagName;

    @Override
    public boolean canFilter(ItemStack itemStack, boolean strict) {
        return super.canFilter(itemStack, strict) && getFinder().modifies(itemStack);
    }

    @Override
    public Finder getFinder() {
        return new TagFinder(tagName);
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("type", 1);
        nbtTags.putString("tagName", tagName);
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        tagName = nbtTags.getString("tagName");
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(1);
        super.write(data);
        data.add(tagName);
    }

    @Override
    protected void read(PacketBuffer dataStream) {
        super.read(dataStream);
        tagName = PacketHandler.readString(dataStream);
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + super.hashCode();
        code = 31 * code + tagName.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof TTagFilter && ((TTagFilter) filter).tagName.equals(tagName);
    }

    @Override
    public TTagFilter clone() {
        TTagFilter filter = new TTagFilter();
        filter.allowDefault = allowDefault;
        filter.color = color;
        filter.tagName = tagName;
        return filter;
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