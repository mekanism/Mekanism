package mekanism.common.content.transporter;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.transporter.Finder.MaterialFinder;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class TMaterialFilter extends TransporterFilter implements IMaterialFilter {

    private ItemStack materialItem = ItemStack.EMPTY;

    public Material getMaterial() {
        return Block.getBlockFromItem(materialItem.getItem()).getStateFromMeta(materialItem.getItemDamage()).getMaterial();
    }

    @Override
    public boolean canFilter(ItemStack itemStack, boolean strict) {
        return super.canFilter(itemStack, strict) && (itemStack.getItem() instanceof BlockItem) && new MaterialFinder(getMaterial()).modifies(itemStack);
    }

    @Override
    public Finder getFinder() {
        return new MaterialFinder(getMaterial());
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("type", 2);
        materialItem.write(nbtTags);
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        materialItem = ItemStack.read(nbtTags);
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(2);

        super.write(data);
        data.add(MekanismUtils.getID(materialItem));
        data.add(materialItem.getCount());
        data.add(materialItem.getItemDamage());
    }

    @Override
    protected void read(ByteBuf dataStream) {
        super.read(dataStream);
        materialItem = new ItemStack(Item.getItemById(dataStream.readInt()), dataStream.readInt(), dataStream.readInt());
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + MekanismUtils.getID(materialItem);
        code = 31 * code + materialItem.getCount();
        code = 31 * code + materialItem.getItemDamage();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof TMaterialFilter && ((TMaterialFilter) filter).materialItem.isItemEqual(materialItem);
    }

    @Override
    public TMaterialFilter clone() {
        TMaterialFilter filter = new TMaterialFilter();
        filter.allowDefault = allowDefault;
        filter.color = color;
        filter.materialItem = materialItem;
        return filter;
    }

    @Nonnull
    @Override
    public ItemStack getMaterialItem() {
        return materialItem;
    }

    @Override
    public void setMaterialItem(@Nonnull ItemStack stack) {
        materialItem = stack;
    }
}