package mekanism.common.content.transporter;

import mekanism.api.TileNetworkList;
import mekanism.common.PacketHandler;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.transporter.Finder.ModIDFinder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class TModIDFilter extends TransporterFilter implements IModIDFilter {

    private String modID;

    @Override
    public boolean canFilter(ItemStack itemStack, boolean strict) {
        return super.canFilter(itemStack, strict) && new ModIDFinder(modID).modifies(itemStack);
    }

    @Override
    public Finder getFinder() {
        return new ModIDFinder(modID);
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("type", 3);
        nbtTags.putString("modID", modID);
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
        modID = dataStream.readString();
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