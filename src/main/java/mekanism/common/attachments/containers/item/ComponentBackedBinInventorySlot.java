
package mekanism.common.attachments.containers.item;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.LockData;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.BinTier;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ComponentBackedBinInventorySlot extends ComponentBackedInventorySlot {

    private final boolean isCreative;

    public static ComponentBackedBinInventorySlot create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int tankIndex) {
        if (!(attachedTo.getItem() instanceof ItemBlockBin item)) {
            throw new IllegalStateException("Attached to should always be a bin item");
        }
        return new ComponentBackedBinInventorySlot(attachedTo, tankIndex, item.getTier());
    }

    private ComponentBackedBinInventorySlot(ItemStack attachedTo, int slotIndex, BinTier tier) {
        super(attachedTo, slotIndex, BasicInventorySlot.alwaysTrueBi, BasicInventorySlot.alwaysTrueBi, BinInventorySlot.validator);
        isCreative = tier == BinTier.CREATIVE;
    }

    @Override
    public ItemStack insertItem(AttachedItems attachedItems, ItemStack current, ItemStack stack, Action action, AutomationType automationType) {
        if (current.isEmpty()) {
            ItemStack lockStack = getLockStack();
            if (!lockStack.isEmpty() && !ItemStack.isSameItemSameComponents(lockStack, stack)) {
                // When locked, we need to make sure the correct item type is being inserted
                return stack;
            } else if (isCreative && action.execute() && automationType != AutomationType.EXTERNAL) {
                //If a player manually inserts into a creative bin, that is empty we need to allow setting the type,
                // Note: We check that it is not external insertion because an empty creative bin acts as a "void" for automation
                ItemStack simulatedRemainder = super.insertItem(attachedItems, current, stack, Action.SIMULATE, automationType);
                if (simulatedRemainder.isEmpty()) {
                    //If we are able to insert it then set perform the action of setting it to full
                    setContents(attachedItems, stack.copyWithCount(getLimit(stack)));
                }
                return simulatedRemainder;
            }
        }
        return super.insertItem(attachedItems, current, stack, action.combine(!isCreative), automationType);
    }

    @Override
    public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        return super.extractItem(amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(AttachedItems, ItemStack, int, Action)}, as both {@link #growStack(int, Action)} and
     * {@link #shrinkStack(int, Action)} are wrapped through this method.
     */
    @Override
    protected int setStackSize(AttachedItems attachedItems, ItemStack current, int amount, Action action) {
        return super.setStackSize(attachedItems, current, amount, action.combine(!isCreative));
    }

    /**
     * Gets the "bottom" stack for the bin, this is the stack that can be extracted/interacted with directly.
     *
     * @return The "bottom" stack for the bin
     *
     * @apiNote The returned stack can be safely modified.
     */
    public ItemStack getBottomStack() {
        //TODO - 1.20.5: ??
        ItemStack current = getStack();
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return current.copyWithCount(Math.min(current.getCount(), current.getMaxStackSize()));
    }

    /**
     * Modifies the lock state of the slot.
     *
     * @param lock if the slot should be locked
     *
     * @return if the lock state was modified
     */
    public boolean setLocked(boolean lock) {//TODO - 1.20.5: ??
        // Don't lock if:
        // - We are a creative bin
        // - We already have the same state as the one we're supposed to switch to
        // - We were asked to lock, but we're empty
        if (isCreative || isLocked() == lock) {
            return false;
        } else if (lock) {
            ItemStack current = getStack();
            if (current.isEmpty()) {
                return false;
            }
            setStack(current);
        } else {
            setLockStack(ItemStack.EMPTY);
        }
        return true;
    }

    /**
     * For use by upgrade recipes, do not use this in place of {@link #setLocked(boolean)}
     */
    public void setLockStack(@NotNull ItemStack stack) {//TODO - 1.20.5: ??
        //Note: This doesn't support the case where one backing stack may have multiple bin slots. If we ever support that case
        // we will need to adjust how this works
        if (stack.isEmpty()) {
            attachedTo.remove(MekanismDataComponents.LOCK);
        } else {
            attachedTo.set(MekanismDataComponents.LOCK, new LockData(stack.copyWithCount(1)));
        }
    }

    public boolean isLocked() {//TODO - 1.20.5: ??
        return !getLockStack().isEmpty();
    }

    public ItemStack getRenderStack() {//TODO - 1.20.5: ??
        ItemStack lockStack = getLockStack();
        return lockStack.isEmpty() ? getStack() : lockStack;
    }

    public ItemStack getLockStack() {//TODO - 1.20.5: ??
        LockData lockData = attachedTo.get(MekanismDataComponents.LOCK);
        return lockData == null ? ItemStack.EMPTY : lockData.lock();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = super.serializeNBT(provider);
        ItemStack lockStack = getLockStack();
        if (!lockStack.isEmpty()) {
            nbt.put(SerializationConstants.LOCK_STACK, lockStack.save(provider));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        NBTUtils.setItemStackOrEmpty(provider, nbt, SerializationConstants.LOCK_STACK, this::setLockStack);
        super.deserializeNBT(provider, nbt);
    }
}