package mekanism.common.content.miner;

import mekanism.api.NBTConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class MinerModIDFilter extends MinerFilter<MinerModIDFilter> implements IModIDFilter<MinerModIDFilter> {

    private String modID;

    @Override
    public boolean canFilter(BlockState state) {
        String id = state.getBlock().getRegistryName().getNamespace();
        if (modID.equals(id) || modID.equals("*")) {
            return true;
        } else if (modID.endsWith("*") && !modID.startsWith("*")) {
            return id.startsWith(modID.substring(0, modID.length() - 1));
        } else if (modID.startsWith("*") && !modID.endsWith("*")) {
            return id.endsWith(modID.substring(1));
        } else if (modID.startsWith("*") && modID.endsWith("*")) {
            return id.contains(modID.substring(1, modID.length() - 1));
        }
        return false;
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
        buffer.writeString(modID);
    }

    @Override
    public void read(PacketBuffer dataStream) {
        super.read(dataStream);
        modID = BasePacketHandler.readString(dataStream);
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + modID.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return filter instanceof MinerModIDFilter && ((MinerModIDFilter) filter).modID.equals(modID);
    }

    @Override
    public MinerModIDFilter clone() {
        MinerModIDFilter filter = new MinerModIDFilter();
        filter.replaceStack = replaceStack;
        filter.requireStack = requireStack;
        filter.modID = modID;
        return filter;
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