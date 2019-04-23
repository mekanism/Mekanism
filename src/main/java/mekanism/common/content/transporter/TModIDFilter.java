package mekanism.common.content.transporter;

import io.netty.buffer.ByteBuf;
import mekanism.common.PacketHandler;
import mekanism.api.TileNetworkList;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.transporter.Finder.ModIDFinder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TModIDFilter extends TransporterFilter implements IModIDFilter {

    private String modID;

    @Override
    public boolean canFilter(ItemStack itemStack, boolean strict) {
        if (itemStack.isEmpty()) {
            return false;
        }

        return new ModIDFinder(modID).modifies(itemStack);
    }

    @Override
    public Finder getFinder() {
        return new ModIDFinder(modID);
    }

    @Override
    public void write(NBTTagCompound nbtTags) {
        super.write(nbtTags);

        nbtTags.setInteger("type", 3);
        nbtTags.setString("modID", modID);
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
        code = 31 * code + super.hashCode();
        code = 31 * code + modID.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof TModIDFilter && ((TModIDFilter) filter).modID.equals(modID);
    }

    @Override
    public TModIDFilter clone() {
        TModIDFilter filter = new TModIDFilter();
        filter.allowDefault = allowDefault;
        filter.color = color;
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
