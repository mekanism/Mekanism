package mekanism.common.content.miner;

import io.netty.buffer.ByteBuf;
import mekanism.api.TileNetworkList;
import mekanism.common.PacketHandler;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.content.transporter.Finder.OreDictFinder;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class MOreDictFilter extends MinerFilter implements IOreDictFilter {

    private String oreDictName;

    @Override
    public boolean canFilter(ItemStack itemStack) {
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BlockItem)) {
            return false;
        }
        return new OreDictFinder(oreDictName).modifies(itemStack);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("type", 1);
        nbtTags.putString("oreDictName", oreDictName);
        return nbtTags;
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
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
        code = 31 * code + oreDictName.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof MOreDictFilter && ((MOreDictFilter) filter).oreDictName.equals(oreDictName);
    }

    @Override
    public MOreDictFilter clone() {
        MOreDictFilter filter = new MOreDictFilter();
        filter.replaceStack = replaceStack;
        filter.requireStack = requireStack;
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