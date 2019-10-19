package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.tier.BinTier;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;

//TODO: Check if we need any more special handling for creative bins
public class BinInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> validator = stack -> !(stack.getItem() instanceof ItemBlockBin);

    public static BinInventorySlot create(IMekanismInventory inventory, BinTier tier) {
        return new BinInventorySlot(inventory, tier);
    }

    private boolean isCreative;

    private BinInventorySlot(IMekanismInventory inventory, BinTier tier) {
        super(tier.getStorage(), alwaysTrue, alwaysTrue, validator, inventory, 0, 0);
        isCreative = tier == BinTier.CREATIVE;
        obeyStackLimit = false;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(@Nonnull ItemStack stack, Action action) {
        return super.insertItem(stack, action.combine(!isCreative));
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int amount, Action action) {
        return super.extractItem(amount, action.combine(!isCreative));
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(int, Action)}, as both {@link #growStack(int, Action)} and {@link #shrinkStack(int, Action)} are wrapped through
     * this method.
     */
    @Override
    public int setStackSize(int amount, Action action) {
        return super.setStackSize(amount, action.combine(!isCreative));
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot(int index) {
        return null;
    }

    //TODO: JavaDoc that the returned stack can be modified
    @Nonnull
    public ItemStack getBottomStack() {
        //TODO: Should we directly access current by just changing it to protected
        ItemStack current = getStack();
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return StackUtils.size(current, Math.min(current.getCount(), current.getMaxStackSize()));
    }
}