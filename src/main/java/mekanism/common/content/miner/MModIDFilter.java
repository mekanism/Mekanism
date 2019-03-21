package mekanism.common.content.miner;

import io.netty.buffer.ByteBuf;
import mekanism.common.PacketHandler;
import mekanism.api.TileNetworkList;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.transporter.Finder.ModIDFinder;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MModIDFilter extends MinerFilter implements IModIDFilter {

    private String modID;

    @Override
    public boolean canFilter(ItemStack itemStack) {
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof ItemBlock)) {
            return false;
        }

        return new ModIDFinder(modID).modifies(itemStack);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound nbtTags) {
        super.write(nbtTags);

        nbtTags.setInteger("type", 3);
        nbtTags.setString("modID", modID);

        return nbtTags;
    }

    @Override
    protected void read(NBTTagCompound nbtTags) {
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
    protected void read(ByteBuf dataStream) {
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
        return super.equals(filter) && filter instanceof MModIDFilter && ((MModIDFilter) filter).modID.equals(modID);
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
