package mekanism.common.content.miner;

import javax.annotation.Nonnull;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IMaterialFilter;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class MinerMaterialFilter extends MinerFilter<MinerMaterialFilter> implements IMaterialFilter<MinerMaterialFilter> {

    private ItemStack materialItem = ItemStack.EMPTY;

    public MinerMaterialFilter(ItemStack item) {
        materialItem = item;
    }

    public MinerMaterialFilter() {
    }

    public MinerMaterialFilter(MinerMaterialFilter filter) {
        super(filter);
        materialItem = filter.materialItem.copy();
    }

    @Override
    public boolean canFilter(BlockState state) {
        return state.getMaterial() == getMaterial();
    }

    @Override
    public boolean hasBlacklistedElement() {
        return !materialItem.isEmpty() && materialItem.getItem() instanceof BlockItem && TagCache.materialHasMinerBlacklisted(materialItem);
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
        return super.equals(filter) && filter instanceof MinerMaterialFilter && ((MinerMaterialFilter) filter).materialItem.sameItem(materialItem);
    }

    @Override
    public MinerMaterialFilter clone() {
        return new MinerMaterialFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.MINER_MATERIAL_FILTER;
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