package mekanism.common.content.miner;

import java.util.Set;
import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class MinerTagFilter extends MinerFilter<MinerTagFilter> implements ITagFilter<MinerTagFilter> {

    private String tagName;

    public MinerTagFilter(String tagName) {
        this.tagName = tagName;
    }

    public MinerTagFilter() {
    }

    @Override
    public boolean canFilter(BlockState state) {
        Set<ResourceLocation> tags = state.getBlock().getTags();
        if (tags.isEmpty()) {
            return false;
        } else if (tagName.equals("*")) {
            //If we have any tags and our filter is everything, allow it to filter it
            return true;
        }
        for (ResourceLocation tag : tags) {
            String tagAsString = tag.toString();
            if (tagName.equals(tagAsString)) {
                return true;
            } else if (tagName.endsWith("*") && !tagName.startsWith("*")) {
                if (tagAsString.startsWith(tagName.substring(0, tagName.length() - 1))) {
                    return true;
                }
            } else if (tagName.startsWith("*") && !tagName.endsWith("*")) {
                if (tagAsString.endsWith(tagName.substring(1))) {
                    return true;
                }
            } else if (tagName.startsWith("*") && tagName.endsWith("*")) {
                if (tagAsString.contains(tagName.substring(1, tagName.length() - 1))) {
                    return true;
                }
            }
        }
        return false;
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
        int code = 1;
        code = 31 * code + tagName.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return filter instanceof MinerTagFilter && ((MinerTagFilter) filter).tagName.equals(tagName);
    }

    @Override
    public MinerTagFilter clone() {
        MinerTagFilter filter = new MinerTagFilter();
        filter.replaceStack = replaceStack;
        filter.requireStack = requireStack;
        filter.tagName = tagName;
        return filter;
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