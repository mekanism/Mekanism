package mekanism.common.attachments.containers.item;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ComponentBackedInventorySlot extends ComponentBackedContainer<ItemStack, AttachedItems> implements IInventorySlot {

    private final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract;
    private final BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert;
    private final Predicate<@NotNull ItemStack> validator;
    private final boolean obeyStackLimit;
    private final int limit;

    public ComponentBackedInventorySlot(ItemStack attachedTo, int slotIndex, BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert, Predicate<@NotNull ItemStack> validator) {
        this(attachedTo, slotIndex, canExtract, canInsert, validator, true, BasicInventorySlot.DEFAULT_LIMIT);
    }

    public ComponentBackedInventorySlot(ItemStack attachedTo, int slotIndex, BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert, Predicate<@NotNull ItemStack> validator, boolean obeyStackLimit, int limit) {
        super(attachedTo, slotIndex);
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.validator = validator;
        this.obeyStackLimit = obeyStackLimit;
        this.limit = limit;
    }

    @Override
    protected Supplier<? extends DataComponentType<AttachedItems>> dataComponentType() {
        return MekanismDataComponents.ATTACHED_ITEMS;
    }

    @Override
    protected ItemStack copy(ItemStack toCopy) {
        return toCopy.copy();
    }

    @Override
    protected boolean isEmpty(ItemStack value) {
        return value.isEmpty();
    }

    @Override
    public ItemStack getStack() {
        //TODO - 1.20.5: Similar to getBasicInventorySlot do we want to reduce calls to this? Probably (We mostly do so, but we probably want to add a note here)
        AttachedItems attachedItems = getAttached();
        return attachedItems == null ? ItemStack.EMPTY : getContents(attachedItems);
    }

    @Override
    public final void setStack(ItemStack stack) {
        setContents(stack);
    }

    /**
     * Ignores current contents
     */
    private boolean isItemValidForInsertion(ItemStack stack, AutomationType automationType) {
        return validator.test(stack) && canInsert.test(stack, automationType);
    }

    @Override
    public final ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
        if (stack.isEmpty()) {
            //"Fail quick" if the given stack is empty
            return ItemStack.EMPTY;
        }
        AttachedItems attachedItems = getAttached();
        if (attachedItems == null) {
            //"Fail quick" if we can't have a stack
            return stack;
        }
        return insertItem(attachedItems, getContents(attachedItems), stack, action, automationType);
    }

    public ItemStack insertItem(AttachedItems attachedItems, ItemStack current, ItemStack stack, Action action, AutomationType automationType) {
        if (stack.isEmpty()) {
            //"Fail quick" if the given stack is empty
            return ItemStack.EMPTY;
        }
        //Validate that we aren't at max stack size before we try to see if we can insert the item, as on average this will be a cheaper check
        int needed = getLimit(stack) - current.getCount();
        if (needed <= 0 || !isItemValidForInsertion(stack, automationType)) {
            //Fail if we are a full slot, or we can never insert the item or currently are unable to insert it
            return stack;
        } else if (current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack)) {
            int toAdd = Math.min(stack.getCount(), needed);
            if (action.execute()) {
                //Note: We let setStack handle updating the backing holding stack
                // We use current.getCount + toAdd so that if we are empty we end up at toAdd
                // but if we aren't then we grow by the given amount
                //TODO - 1.20.5: FIXME?? If same type we want to add it by x
                setContents(attachedItems, stack.copyWithCount(current.getCount() + toAdd));
            }
            return stack.copyWithCount(stack.getCount() - toAdd);
        }
        //If we didn't accept this item, then just return the given stack
        return stack;
    }

    @Override
    public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        if (amount < 1) {
            //"Fail quick" if we don't can never extract from this slot, have an item stored, or the amount being requested is less than one
            return ItemStack.EMPTY;
        }
        AttachedItems attachedItems = getAttached();
        if (attachedItems == null) {
            //"Fail quick" if we can't have a stack
            return ItemStack.EMPTY;
        }
        ItemStack current = getContents(attachedItems);
        if (current.isEmpty() || !canExtract.test(current, automationType)) {
            return ItemStack.EMPTY;
        }
        //Ensure that if this slot allows going past the max stack size of an item, that when extracting we don't act as if we have more than
        // the max stack size, as the JavaDoc for IItemHandler requires that the returned stack is not larger than its stack size
        int currentAmount = Math.min(current.getCount(), current.getMaxStackSize());
        if (currentAmount < amount) {
            //If we are trying to extract more than we have, just change it so that we are extracting it all
            amount = currentAmount;
        }
        //Note: While we technically could just return the stack itself if we are removing all that we have, it would require a lot more checks
        // especially for supporting the fact of limiting by the max stack size.
        ItemStack toReturn = current.copyWithCount(amount);
        if (action.execute()) {
            //Note: We let setStack handle updating the backing holding stack
            setContents(attachedItems, current.copyWithCount(current.getCount() - amount));
        }
        return toReturn;
    }

    @Override
    public int getLimit(ItemStack stack) {
        return obeyStackLimit && !stack.isEmpty() ? Math.min(limit, stack.getMaxStackSize()) : limit;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return validator.test(stack);
    }

    @Override
    public final int setStackSize(int amount, Action action) {
        AttachedItems attachedItems = getAttached();
        if (attachedItems == null) {
            //"Fail quick" if we can't have a stack
            return 0;
        }
        return setStackSize(attachedItems, getContents(attachedItems), amount, action);
    }

    protected int setStackSize(AttachedItems attachedItems, ItemStack current, int amount, Action action) {
        if (current.isEmpty()) {
            return 0;
        } else if (amount <= 0) {
            if (action.execute()) {
                setContents(attachedItems, ItemStack.EMPTY);
            }
            return 0;
        }
        int maxStackSize = getLimit(current);
        if (amount > maxStackSize) {
            amount = maxStackSize;
        }
        if (current.getCount() == amount || action.simulate()) {
            //If our size is not changing, or we are only simulating the change, don't do anything
            return amount;
        }
        setContents(attachedItems, current.copyWithCount(amount));
        return amount;
    }

    @Override
    public int growStack(int amount, Action action) {
        AttachedItems attachedItems = getAttached();
        if (attachedItems == null) {
            //"Fail quick" if we can't have a stack
            return 0;
        }
        //Avoid extra getStack lookup calls
        ItemStack stack = getContents(attachedItems);
        int current = stack.getCount();
        if (amount > 0) {
            //Cap adding amount at how much we need, so that we don't risk integer overflow
            amount = Math.min(amount, getLimit(stack));
        }
        int newSize = setStackSize(attachedItems, stack, current + amount, action);
        return newSize - current;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        //TODO - 1.20.5: This is a copy of BasicInventorySlot#serializeNBT. We might need to also grab the specific overrides of
        // that method as special component backed inventory slots, that then access and put that other data as a different component?
        CompoundTag nbt = new CompoundTag();
        ItemStack current = getStack();
        if (!current.isEmpty()) {
            nbt.put(NBTConstants.ITEM, current.save(provider));
            if (getCount() > current.getMaxStackSize()) {
                nbt.putInt(NBTConstants.SIZE_OVERRIDE, getCount());
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        ItemStack stack = ItemStack.EMPTY;
        if (nbt.contains(NBTConstants.ITEM, Tag.TAG_COMPOUND)) {
            stack = ItemStack.parseOptional(provider, nbt.getCompound(NBTConstants.ITEM));
            NBTUtils.setIntIfPresent(nbt, NBTConstants.SIZE_OVERRIDE, stack::setCount);
        }
        setStack(stack);
    }
}