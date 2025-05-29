package mekanism.common.content.miner;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.tags.MekanismTags;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MinerItemStackFilter extends MinerFilter<MinerItemStackFilter> implements IItemStackFilter<MinerItemStackFilter> {

    public static final MapCodec<MinerItemStackFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> baseMinerCodec(instance)
          .and(ItemStack.SINGLE_ITEM_CODEC.fieldOf(SerializationConstants.TARGET_STACK).forGetter(MinerItemStackFilter::getItemStack))
          .apply(instance, MinerItemStackFilter::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MinerItemStackFilter> STREAM_CODEC = StreamCodec.composite(
          baseMinerStreamCodec(MinerItemStackFilter::new), Function.identity(),
          ItemStack.OPTIONAL_STREAM_CODEC, MinerItemStackFilter::getItemStack,
          (filter, itemType) -> {
              filter.itemType = itemType;
              return filter;
          }
    );

    private ItemStack itemType = ItemStack.EMPTY;

    public MinerItemStackFilter() {
    }

    protected MinerItemStackFilter(boolean enabled, Item replaceTarget, boolean requiresReplacement, ItemStack itemType) {
        super(enabled, replaceTarget, requiresReplacement);
        this.itemType = itemType;
    }

    public MinerItemStackFilter(MinerItemStackFilter filter) {
        super(filter);
        itemType = filter.itemType.copy();
    }

    @Override
    public boolean canFilter(BlockState state) {
        ItemStack itemStack = new ItemStack(state.getBlock());
        if (itemStack.isEmpty()) {
            return false;
        }
        return itemType.is(itemStack.getItemHolder());
    }

    @Override
    public boolean hasBlacklistedElement() {
        return !itemType.isEmpty() && itemType.getItem() instanceof BlockItem blockItem && blockItem.getBlock().builtInRegistryHolder().is(MekanismTags.Blocks.MINER_BLACKLIST);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + itemType.getItem().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        return itemType.getItem() == ((MinerItemStackFilter) o).itemType.getItem();
    }

    @Override
    public MinerItemStackFilter clone() {
        return new MinerItemStackFilter(this);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.MINER_ITEMSTACK_FILTER;
    }

    @NotNull
    @Override
    public ItemStack getItemStack() {
        return itemType;
    }

    @Override
    public void setItemStack(@NotNull ItemStack stack) {
        itemType = stack;
    }
}