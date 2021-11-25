package mekanism.common.content.miner;

import mekanism.api.NBTConstants;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class MinerTagFilter extends MinerFilter<MinerTagFilter> implements ITagFilter<MinerTagFilter> {

    private String tagName;

    public MinerTagFilter(String tagName) {
        this.tagName = tagName;
    }

    public MinerTagFilter() {
    }

    public MinerTagFilter(MinerTagFilter filter) {
        super(filter);
        tagName = filter.tagName;
    }

    @Override
    public boolean canFilter(BlockState state) {
        return state.getBlock().getTags().stream().anyMatch(tag -> WildcardMatcher.matches(tagName, tag.toString()));
    }

    @Override
    public boolean hasBlacklistedElement() {
        return TagCache.tagHasMinerBlacklisted(tagName);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putString(NBTConstants.TAG_NAME, tagName);
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        tagName = nbtTags.getString(NBTConstants.TAG_NAME);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeUtf(tagName);
    }

    @Override
    public void read(PacketBuffer dataStream) {
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
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof MinerTagFilter && ((MinerTagFilter) filter).tagName.equals(tagName);
    }

    @Override
    public MinerTagFilter clone() {
        return new MinerTagFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.MINER_TAG_FILTER;
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