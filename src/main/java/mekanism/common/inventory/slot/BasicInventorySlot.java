package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.ItemHandlerHelper;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicInventorySlot implements IInventorySlot {

    public static final Predicate<@NonNull ItemStack> alwaysTrue = stack -> true;
    public static final Predicate<@NonNull ItemStack> alwaysFalse = stack -> false;
    public static final BiPredicate<@NonNull ItemStack, @NonNull AutomationType> alwaysTrueBi = (stack, automationType) -> true;
    public static final BiPredicate<@NonNull ItemStack, @NonNull AutomationType> manualOnly = (stack, automationType) -> automationType == AutomationType.MANUAL;
    public static final BiPredicate<@NonNull ItemStack, @NonNull AutomationType> internalOnly = (stack, automationType) -> automationType == AutomationType.INTERNAL;
    public static final BiPredicate<@NonNull ItemStack, @NonNull AutomationType> notExternal = (stack, automationType) -> automationType != AutomationType.EXTERNAL;
    private static final int DEFAULT_LIMIT = 64;

    public static BasicInventorySlot at(@Nullable IMekanismInventory inventory, int x, int y) {
        return at(alwaysTrue, inventory, x, y);
    }

    public static BasicInventorySlot at(Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(validator, "Item validity check cannot be null");
        return new BasicInventorySlot(alwaysTrueBi, alwaysTrueBi, validator, inventory, x, y);
    }

    public static BasicInventorySlot at(Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new BasicInventorySlot(canExtract, canInsert, alwaysTrue, inventory, x, y);
    }

    public static BasicInventorySlot at(BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract, BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert,
          @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new BasicInventorySlot(canExtract, canInsert, alwaysTrue, inventory, x, y);
    }

    /**
     * @apiNote This is only protected for direct querying access. To modify this stack the external methods or {@link #setStackUnchecked(ItemStack)} should be used
     * instead.
     */
    protected ItemStack current = ItemStack.EMPTY;
    private final BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract;
    private final BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert;
    private final Predicate<@NonNull ItemStack> validator;
    private final int limit;
    @Nullable
    private final IMekanismInventory inventory;
    private final int x;
    private final int y;
    protected boolean obeyStackLimit = true;
    private ContainerSlotType slotType = ContainerSlotType.NORMAL;
    @Nullable
    private SlotOverlay slotOverlay;

    protected BasicInventorySlot(Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator,
          @Nullable IMekanismInventory inventory, int x, int y) {
        //TODO: Re-evaluate this
        this((stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack),
              validator, inventory, x, y);
    }

    protected BasicInventorySlot(BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract, BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        this(DEFAULT_LIMIT, canExtract, canInsert, validator, inventory, x, y);
    }

    protected BasicInventorySlot(int limit, BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract, BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert,
          Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        this.limit = limit;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.inventory = inventory;
        this.x = x;
        this.y = y;
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote We return a cached value from this that if modified won't actually end up having any information about the slot get changed.
     */
    @Override
    public ItemStack getStack() {
        //TODO: Should we return a copy to ensure that our stack is not modified, we could cache our copy and only update it at given times
        //TODO: YES it will help expose bugs, and we need to make sure that we are not calling shrink/grow on anything we should not be
        // Though it would be "cleaner" to not have to especially in terms of for finding bugs when API is being mistreated.
        // Would be nice to extend ItemStack to have one that throws a warning/error on being modified
        //TODO: If we do have a cached copy, then when this is called we may want to log a warning that something tried to modify the stack
        // if the copy has different information/size than our actual stored stack?? Would need to make sure the cached version stays up to date
        return current;
    }

    @Override
    public void setStack(ItemStack stack) {
        //TODO: Should we allow forcefully setting invalid items? At least we need to go through them and check to make sure we allow setting an empty container??
        // This error of empty container not being valid may not even be an issue once we move logic for resources into the specific slots
        setStack(stack, true);
    }

    protected void setStackUnchecked(ItemStack stack) {
        setStack(stack, false);
    }

    private void setStack(ItemStack stack, boolean validateStack) {
        //TODO: Decide if we want to limit this to the slots limit and maybe make a method for reading from file that lets it go past the limit??
        if (stack.isEmpty()) {
            current = ItemStack.EMPTY;
        } else if (!validateStack || isItemValid(stack)) {
            current = stack.copy();
        } else {
            //Throws a RuntimeException as IItemHandlerModifiable specifies is allowed when something unexpected happens
            // As setStack is more meant to be used as an internal method
            throw new RuntimeException("Invalid stack for slot: " + stack.getItem().getRegistryName() + " " + stack.getCount() + " " + stack.getTag());
        }
        onContentsChanged();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
        if (stack.isEmpty() || !isItemValid(stack) || !canInsert.test(stack, automationType)) {
            //"Fail quick" if the given stack is empty or we can never insert the item or currently are unable to insert it
            return stack;
        }
        int needed = getLimit(stack) - getCount();
        if (needed <= 0) {
            //Fail if we are a full slot
            return stack;
        }
        boolean sameType = false;
        if (isEmpty() || (sameType = ItemHandlerHelper.canItemStacksStack(current, stack))) {
            int toAdd = Math.min(stack.getCount(), needed);
            if (action.execute()) {
                //If we want to actually insert the item, then update the current item
                if (sameType) {
                    //We can just grow our stack by the amount we want to increase it
                    current.grow(toAdd);
                    onContentsChanged();
                } else {
                    //If we are not the same type then we have to copy the stack and set it
                    // Just set it unchecked as we have already validated it
                    // Note: this also will mark that the contents changed
                    setStackUnchecked(StackUtils.size(stack, toAdd));
                }
            }
            return StackUtils.size(stack, stack.getCount() - toAdd);
        }
        //If we didn't accept this item, then just return the given stack
        return stack;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount < 1 || !canExtract.test(current, automationType)) {
            //"Fail quick" if we don't can never extract from this slot, have an item stored, or the amount being requested is less than one
            return ItemStack.EMPTY;
        }
        //Ensure that if this slot allows going past the max stack size of an item, that when extracting we don't act as if we have more than
        // the max stack size, as the JavaDoc for IItemHandler requires that the returned stack is not larger than its stack size
        int currentAmount = Math.min(getCount(), current.getMaxStackSize());
        if (currentAmount < amount) {
            //If we are trying to extract more than we have, just change it so that we are extracting it all
            amount = currentAmount;
        }
        //Note: While we technically could just return the stack itself if we are removing all that we have, it would require a lot more checks
        // especially for supporting the fact of limiting by the max stack size.
        ItemStack toReturn = StackUtils.size(current, amount);
        if (action.execute()) {
            //If shrink gets the size to zero it will update the empty state so that isEmpty() returns true.
            current.shrink(amount);
            onContentsChanged();
        }
        return toReturn;
    }

    //TODO: Evaluate usages of this maybe some should be capped by the max size of the stack
    // In fact most uses of this probably can instead use the insertItem method instead
    @Override
    public int getLimit(ItemStack stack) {
        //TODO: is this a decent way to do this or do we want to set obeyStack limit some other way
        return obeyStackLimit && !stack.isEmpty() ? Math.min(limit, stack.getMaxStackSize()) : limit;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return validator.test(stack);
    }

    @Override
    public void onContentsChanged() {
        if (inventory != null) {
            inventory.onContentsChanged();
        }
    }

    //TODO: Should we move InventoryContainerSlot to the API and reference that instead
    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return new InventoryContainerSlot(this, x, y, slotType, slotOverlay);
    }

    public void setSlotType(ContainerSlotType slotType) {
        this.slotType = slotType;
    }

    public void setSlotOverlay(@Nullable SlotOverlay slotOverlay) {
        this.slotOverlay = slotOverlay;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public int setStackSize(int amount, Action action) {
        if (isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setStack(ItemStack.EMPTY);
            }
            return 0;
        }
        int maxStackSize = getLimit(current);
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (getCount() == amount || action.simulate()) {
            //If our size is not changing or we are only simulating the change, don't do anything
            return amount;
        }
        current.setCount(amount);
        onContentsChanged();
        return amount;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public boolean isEmpty() {
        return current.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getStack()}, and we can optimize out the copying, and can also directly modify our stack
     * instead of having to make a copy.
     */
    @Override
    public int getCount() {
        return current.getCount();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (!isEmpty()) {
            nbt.put(NBTConstants.ITEM, current.write(new CompoundNBT()));
            if (getCount() > current.getMaxStackSize()) {
                nbt.putInt(NBTConstants.SIZE_OVERRIDE, getCount());
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        ItemStack stack = ItemStack.EMPTY;
        if (nbt.contains(NBTConstants.ITEM, NBT.TAG_COMPOUND)) {
            stack = ItemStack.read(nbt.getCompound(NBTConstants.ITEM));
            NBTUtils.setIntIfPresent(nbt, NBTConstants.SIZE_OVERRIDE, stack::setCount);
        }
        //Directly set the stack in case the item is no longer valid for the stack.
        // We do this instead of using setStackUnchecked to avoid calling markDirty when we are loading
        // the inventory and the world is still null on the tile
        setStackUnchecked(stack);
    }
}