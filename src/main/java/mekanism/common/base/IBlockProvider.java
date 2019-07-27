package mekanism.common.base;

import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public interface IBlockProvider extends IItemProvider {

    @Nonnull
    Block getBlock();

    default boolean blockMatches(ItemStack otherStack) {
        Item item = otherStack.getItem();
        return item instanceof ItemBlock && blockMatches(((ItemBlock) item).getBlock());
    }

    default boolean blockMatches(Block other) {
        return getBlock() == other;
    }
}