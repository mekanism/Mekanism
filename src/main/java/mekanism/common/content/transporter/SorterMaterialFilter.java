package mekanism.common.content.transporter;

import javax.annotation.Nonnull;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.lib.inventory.Finder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class SorterMaterialFilter extends SorterFilter<SorterMaterialFilter> implements IMaterialFilter<SorterMaterialFilter> {

    private ItemStack materialItem = ItemStack.EMPTY;

    public SorterMaterialFilter() {
    }

    public SorterMaterialFilter(SorterMaterialFilter filter) {
        super(filter);
        materialItem = filter.materialItem.copy();
    }

    @Override
    public Finder getFinder() {
        return Finder.material(getMaterial());
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        materialItem.save(nbtTags);
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        materialItem = ItemStack.of(nbtTags);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeItem(materialItem);
    }

    @Override
    public void read(PacketBuffer dataStream) {
        super.read(dataStream);
        materialItem = dataStream.readItem();
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        code = 31 * code + materialItem.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object filter) {
        return super.equals(filter) && filter instanceof SorterMaterialFilter && ((SorterMaterialFilter) filter).materialItem.sameItem(materialItem);
    }

    @Override
    public SorterMaterialFilter clone() {
        return new SorterMaterialFilter(this);
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