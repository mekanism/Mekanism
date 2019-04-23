package mekanism.common.content.transporter;

import io.netty.buffer.ByteBuf;
import mekanism.common.PacketHandler;
import mekanism.api.TileNetworkList;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.content.transporter.Finder.OreDictFinder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TOreDictFilter extends TransporterFilter implements IOreDictFilter {

    private String oreDictName;

    @Override
    public boolean canFilter(ItemStack itemStack, boolean strict) {
        if (itemStack.isEmpty()) {
            return false;
        }

        return new OreDictFinder(oreDictName).modifies(itemStack);
    }

    @Override
    public Finder getFinder() {
        return new OreDictFinder(oreDictName);
    }

    @Override
    public void write(NBTTagCompound nbtTags) {
        super.write(nbtTags);

        nbtTags.setInteger("type", 1);
        nbtTags.setString("oreDictName", oreDictName);
    }

    @Override
    protected void read(NBTTagCompound nbtTags) {
        super.read(nbtTags);

        oreDictName = nbtTags.getString("oreDictName");
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(1);

        super.write(data);

        data.add(oreDictName);
    }

    @Override
    protected void read(ByteBuf dataStream) {
        super.read(dataStream);

        oreDictName = PacketHandler.readString(dataStream);
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + super.hashCode();
        code = 31 * code + oreDictName.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof TOreDictFilter && ((TOreDictFilter) filter).oreDictName
              .equals(oreDictName);
    }

    @Override
    public TOreDictFilter clone() {
        TOreDictFilter filter = new TOreDictFilter();
        filter.allowDefault = allowDefault;
        filter.color = color;
        filter.oreDictName = oreDictName;

        return filter;
    }

    @Override
    public void setOreDictName(String name) {
        oreDictName = name;
    }

    @Override
    public String getOreDictName() {
        return oreDictName;
    }
}
