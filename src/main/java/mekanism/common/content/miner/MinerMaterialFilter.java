package mekanism.common.content.miner;

import mekanism.common.base.TagCache;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IMaterialFilter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

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
        return super.equals(o) && o instanceof MinerMaterialFilter filter && filter.materialItem.sameItem(materialItem);
    }

    @Override
    public MinerMaterialFilter clone() {
        return new MinerMaterialFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.MINER_MATERIAL_FILTER;
    }

    @NotNull
    @Override
    public ItemStack getMaterialItem() {
        return materialItem;
    }

    @Override
    public void setMaterialItem(@NotNull ItemStack stack) {
        materialItem = stack;
    }
}