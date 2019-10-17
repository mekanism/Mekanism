package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.tier.BinTier;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;

//TODO: Special handling for creative??
public class BinInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> validator = stack -> !(stack.getItem() instanceof ItemBlockBin);

    public static BinInventorySlot create(BinTier tier) {
        return new BinInventorySlot(tier);
    }

    private BinInventorySlot(BinTier tier) {
        //TODO: I don't believe we need to check the itemType or if they can stack as that stuff will be checked when attempting to set it
        super(tier.getStorage(), alwaysTrue, alwaysTrue, validator, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot(int index) {
        return null;
    }

    //TODO: JavaDoc that the returned stack can be mutable or whatever
    @Nonnull
    public ItemStack getBottomStack() {
        //TODO: Should we directly access current
        ItemStack current = getStack();
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return StackUtils.size(current, Math.min(current.getCount(), current.getMaxStackSize()));
    }
}