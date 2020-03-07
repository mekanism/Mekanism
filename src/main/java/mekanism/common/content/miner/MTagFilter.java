package mekanism.common.content.miner;

import java.util.Set;
import mekanism.api.NBTConstants;
import mekanism.api.TileNetworkList;
import mekanism.common.PacketHandler;
import mekanism.common.content.filter.ITagFilter;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class MTagFilter extends MinerFilter<MTagFilter> implements ITagFilter<MTagFilter> {

    private String tagName;

    @Override
    public boolean canFilter(BlockState state) {
        Set<ResourceLocation> tags = state.getBlock().getTags();
        if (tags.isEmpty()) {
            return false;
        }
        for (ResourceLocation tag : tags) {
            String tagAsString = tag.toString();
            if (tagName.equals(tagAsString) || tagName.equals("*")) {
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
        nbtTags.putInt(NBTConstants.TYPE, 1);
        nbtTags.putString(NBTConstants.TAG_NAME, tagName);
        return nbtTags;
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        tagName = nbtTags.getString(NBTConstants.TAG_NAME);
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
        code = 31 * code + tagName.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return filter instanceof MTagFilter && ((MTagFilter) filter).tagName.equals(tagName);
    }

    @Override
    public MTagFilter clone() {
        MTagFilter filter = new MTagFilter();
        filter.replaceStack = replaceStack;
        filter.requireStack = requireStack;
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