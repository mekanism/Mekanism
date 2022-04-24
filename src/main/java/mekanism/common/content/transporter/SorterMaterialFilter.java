package mekanism.common.content.transporter;

import javax.annotation.Nonnull;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.lib.inventory.Finder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

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
    public CompoundTag write(CompoundTag nbtTags) {
        super.write(nbtTags);
        materialItem.save(nbtTags);
        return nbtTags;
    }

    @Override
    public void read(CompoundTag nbtTags) {
        super.read(nbtTags);
        materialItem = ItemStack.of(nbtTags);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeItem(materialItem);
    }

    @Override
    public void read(FriendlyByteBuf dataStream) {
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
    public boolean equals(Object o) {
        return super.equals(o) && o instanceof SorterMaterialFilter filter && filter.materialItem.sameItem(materialItem);
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