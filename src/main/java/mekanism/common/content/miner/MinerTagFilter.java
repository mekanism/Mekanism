package mekanism.common.content.miner;

import mekanism.api.NBTConstants;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

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
        return state.getTags().anyMatch(tag -> WildcardMatcher.matches(tagName, tag));
    }

    @Override
    public boolean hasBlacklistedElement() {
        return TagCache.tagHasMinerBlacklisted(tagName);
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
        return super.equals(o) && o instanceof MinerTagFilter filter && filter.tagName.equals(tagName);
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