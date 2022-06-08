package mekanism.common.content.miner;

import mekanism.api.NBTConstants;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.RegistryUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

public class MinerModIDFilter extends MinerFilter<MinerModIDFilter> implements IModIDFilter<MinerModIDFilter> {

    private String modID;

    public MinerModIDFilter(String modID) {
        this.modID = modID;
    }

    public MinerModIDFilter() {
    }

    public MinerModIDFilter(MinerModIDFilter filter) {
        super(filter);
        modID = filter.modID;
    }

    @Override
    public boolean canFilter(BlockState state) {
        return WildcardMatcher.matches(modID, RegistryUtils.getNamespace(state.getBlock()));
    }

    @Override
    public boolean hasBlacklistedElement() {
        return TagCache.modIDHasMinerBlacklisted(modID);
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
        return super.equals(o) && o instanceof MinerModIDFilter filter && filter.modID.equals(modID);
    }

    @Override
    public MinerModIDFilter clone() {
        return new MinerModIDFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.MINER_MODID_FILTER;
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