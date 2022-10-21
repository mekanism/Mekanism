package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.tier.BinTier;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BinInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NotNull ItemStack> validator = stack -> !(stack.getItem() instanceof ItemBlockBin);

    public static BinInventorySlot create(@Nullable IContentsListener listener, BinTier tier) {
        Objects.requireNonNull(tier, "Bin tier cannot be null");
        return new BinInventorySlot(listener, tier);
    }

    private final boolean isCreative;
    private ItemStack lockStack = ItemStack.EMPTY;

    private BinInventorySlot(@Nullable IContentsListener listener, BinTier tier) {
        super(tier.getStorage(), alwaysTrueBi, alwaysTrueBi, validator, listener, 0, 0);
        isCreative = tier == BinTier.CREATIVE;
        obeyStackLimit = false;
    }

    @Override
    public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
        if (isEmpty()) {
            if (isLocked() && !ItemHandlerHelper.canItemStacksStack(lockStack, stack)) {
                // When locked, we need to make sure the correct item type is being inserted
                return stack;
            } else if (isCreative && action.execute() && automationType != AutomationType.EXTERNAL) {
                //If a player manually inserts into a creative bin, that is empty we need to allow setting the type,
                // Note: We check that it is not external insertion because an empty creative bin acts as a "void" for automation
                ItemStack simulatedRemainder = super.insertItem(stack, Action.SIMULATE, automationType);
                if (simulatedRemainder.isEmpty()) {
                    //If we are able to insert it then set perform the action of setting it to full
                    setStackUnchecked(StackUtils.size(stack, getLimit(stack)));
                }
                return simulatedRemainder;
            }
        }
        return super.insertItem(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        return super.extractItem(amount, action.combine(!isCreative), automationType);
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
    public InventoryContainerSlot createContainerSlot() {
        return null;
    }

    /**
     * Gets the "bottom" stack for the bin, this is the stack that can be extracted/interacted with directly.
     *
     * @return The "bottom" stack for the bin
     *
     * @apiNote The returned stack can be safely modified.
     */
    public ItemStack getBottomStack() {
        if (isEmpty()) {
            return ItemStack.EMPTY;
        }
        return StackUtils.size(current, Math.min(getCount(), current.getMaxStackSize()));
    }

    /**
     * Modifies the lock state of the slot.
     *
     * @param lock if the slot should be locked
     *
     * @return if the lock state was modified
     */
    public boolean setLocked(boolean lock) {
        // Don't lock if:
        // - We are a creative bin
        // - We already have the same state as the one we're supposed to switch to
        // - We were asked to lock, but we're empty
        if (isCreative || isLocked() == lock || (lock && isEmpty())) {
            return false;
        }
        lockStack = lock ? StackUtils.size(current, 1) : ItemStack.EMPTY;
        return true;
    }

    /**
     * For use by upgrade recipes, do not use this in place of {@link #setLocked(boolean)}
     */
    public void setLockStack(@NotNull ItemStack stack) {
        lockStack = StackUtils.size(stack, 1);
    }

    public boolean isLocked() {
        return !lockStack.isEmpty();
    }

    public ItemStack getRenderStack() {
        return isLocked() ? getLockStack() : getStack();
    }

    public ItemStack getLockStack() {
        return lockStack;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        if (isLocked()) {
            nbt.put(NBTConstants.LOCK_STACK, lockStack.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        NBTUtils.setItemStackOrEmpty(nbt, NBTConstants.LOCK_STACK, s -> this.lockStack = s);
        super.deserializeNBT(nbt);
    }
}