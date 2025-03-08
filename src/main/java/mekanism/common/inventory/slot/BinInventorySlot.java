package mekanism.common.inventory.slot;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.tier.BinTier;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BinInventorySlot extends BasicInventorySlot {

    public static final Predicate<@NotNull ItemStack> validator = stack -> !(stack.getItem() instanceof ItemBlockBin);

    @Nullable
    public static ComponentBackedBinInventorySlot getForStack(@NotNull ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemBlockBin) {
            IMekanismInventory attachment = ContainerType.ITEM.createHandler(stack);
            if (attachment != null) {
                List<IInventorySlot> slots = attachment.getInventorySlots(null);
                if (slots.size() == 1) {
                    IInventorySlot slot = slots.getFirst();
                    if (slot instanceof ComponentBackedBinInventorySlot binSlot) {
                        return binSlot;
                    }
                }
            }
        }
        return null;
    }

    public static BinInventorySlot create(@Nullable IContentsListener listener, BinTier tier) {
        Objects.requireNonNull(tier, "Bin tier cannot be null");
        return new BinInventorySlot(listener, tier);
    }

    private final boolean isCreative;
    private ItemStack lockStack = ItemStack.EMPTY;

    private BinInventorySlot(@Nullable IContentsListener listener, BinTier tier) {
        super(tier.getStorage(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, listener, 0, 0);
        isCreative = tier == BinTier.CREATIVE;
        obeyStackLimit = false;
    }

    @Override
    public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
        if (isEmpty()) {
            if (isLocked() && !ItemStack.isSameItemSameComponents(lockStack, stack)) {
                // When locked, we need to make sure the correct item type is being inserted
                return stack;
            } else if (isCreative && action.execute() && automationType != AutomationType.EXTERNAL) {
                //If a player manually inserts into a creative bin, that is empty we need to allow setting the type,
                // Note: We check that it is not external insertion because an empty creative bin acts as a "void" for automation
                ItemStack simulatedRemainder = super.insertItem(stack, Action.SIMULATE, automationType);
                if (simulatedRemainder.isEmpty()) {
                    //If we are able to insert it then set perform the action of setting it to full
                    setStackUnchecked(stack.copyWithCount(getLimit(stack)));
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
        return current.copyWithCount(Math.min(getCount(), current.getMaxStackSize()));
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
        lockStack = lock ? current.copyWithCount(1) : ItemStack.EMPTY;
        return true;
    }

    /**
     * For use by tier installers and parsing placement data, do not use this in place of {@link #setLocked(boolean)}
     */
    public void setLockStack(@NotNull ItemStack stack) {
        lockStack = stack.copyWithCount(1);
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
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        //Note: While we are able to store this extra data for saving and stuff, when converting to an item we need to have
        // the tile copy the lock stack as a component
        CompoundTag nbt = super.serializeNBT(provider);
        if (isLocked()) {
            nbt.put(SerializationConstants.LOCK_STACK, lockStack.save(provider));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        NBTUtils.setItemStackOrEmpty(provider, nbt, SerializationConstants.LOCK_STACK, s -> this.lockStack = s);
        super.deserializeNBT(provider, nbt);
    }
}