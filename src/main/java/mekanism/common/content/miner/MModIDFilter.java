package mekanism.common.content.miner;

import mekanism.api.TileNetworkList;
import mekanism.common.PacketHandler;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.transporter.Finder.ModIDFinder;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class MModIDFilter extends MinerFilter<MModIDFilter> implements IModIDFilter<MModIDFilter> {

    private String modID;

    @Override
    public boolean canFilter(ItemStack itemStack) {
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BlockItem)) {
            return false;
        }
        return new ModIDFinder(modID).modifies(itemStack);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("type", 3);
        nbtTags.putString("modID", modID);
        return nbtTags;
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        modID = nbtTags.getString("modID");
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(3);
        super.write(data);
        data.add(modID);
    }

    @Override
    protected void read(PacketBuffer dataStream) {
        super.read(dataStream);
        modID = PacketHandler.readString(dataStream);
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + modID.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return filter instanceof MModIDFilter && ((MModIDFilter) filter).modID.equals(modID);
    }

    @Override
    public MModIDFilter clone() {
        MModIDFilter filter = new MModIDFilter();
        filter.replaceStack = replaceStack;
        filter.requireStack = requireStack;
        filter.modID = modID;
        return filter;
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