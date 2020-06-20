package mekanism.common.content.transporter;

import javax.annotation.Nonnull;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.lib.inventory.Finder;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class SorterMaterialFilter extends SorterFilter<SorterMaterialFilter> implements IMaterialFilter<SorterMaterialFilter> {

    private ItemStack materialItem = ItemStack.EMPTY;

    public Material getMaterial() {
        return Block.getBlockFromItem(materialItem.getItem()).getDefaultState().getMaterial();
    }

    @Override
    public Finder getFinder() {
        return Finder.material(getMaterial());
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        materialItem.write(nbtTags);
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        materialItem = ItemStack.read(nbtTags);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeItemStack(materialItem);
    }

    @Override
    public void read(PacketBuffer dataStream) {
        super.read(dataStream);
        materialItem = dataStream.readItemStack();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + super.hashCode();
        code = 31 * code + materialItem.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof SorterMaterialFilter && ((SorterMaterialFilter) filter).materialItem.isItemEqual(materialItem);
    }

    @Override
    public SorterMaterialFilter clone() {
        SorterMaterialFilter filter = new SorterMaterialFilter();
        filter.allowDefault = allowDefault;
        filter.color = color;
        filter.materialItem = materialItem;
        return filter;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SORTER_MATERIAL_FILTER;
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