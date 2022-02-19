package mekanism.common.inventory.container.slot;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

//Like net.minecraftforge.items.SlotItemHandler, except directly interacts with the IInventorySlot instead
public class InventoryContainerSlot extends Slot implements IInsertableSlot {

    private static final IInventory emptyInventory = new Inventory(0);
    private final Consumer<ItemStack> uncheckedSetter;
    private final ContainerSlotType slotType;
    private final BasicInventorySlot slot;
    @Nullable
    private final SlotOverlay slotOverlay;

    public InventoryContainerSlot(BasicInventorySlot slot, int x, int y, ContainerSlotType slotType, @Nullable SlotOverlay slotOverlay,
          Consumer<ItemStack> uncheckedSetter) {
        super(emptyInventory, 0, x, y);
        this.slot = slot;
        this.slotType = slotType;
        this.slotOverlay = slotOverlay;
        this.uncheckedSetter = uncheckedSetter;
    }

    public IInventorySlot getInventorySlot() {
        return slot;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(@Nonnull ItemStack stack, Action action) {
        ItemStack remainder = slot.insertItem(stack, action, AutomationType.MANUAL);
        if (action.execute() && stack.getCount() != remainder.getCount()) {
            setChanged();
        }
        return remainder;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (slot.isEmpty()) {
            //If the slot is currently empty, just try simulating the insertion
            return insertItem(stack, Action.SIMULATE).getCount() < stack.getCount();
        }
        //Otherwise, we need to check if we can extract the current item
        if (slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).isEmpty()) {
            //If we can't, fail
            return false;
        }
        //If we can check if we can insert the item ignoring the current contents
        return slot.isItemValidForInsertion(stack, AutomationType.MANUAL);
    }

    @Nonnull
    @Override
    public ItemStack getItem() {
        return slot.getStack();
    }

    @Override
    public boolean hasItem() {
        return !slot.isEmpty();
    }

    @Override
    public void set(@Nonnull ItemStack stack) {
        //Note: We have to set the stack in an unchecked manner here, so that if we sync a stack from the server to the client that
        // the client does not think is valid for the stack, it doesn't cause major issues. Additionally, we do this directly in
        // our putStack method rather than having a separate unchecked method, as if some modder is modifying inventories directly
        // for some reason, and the machine has invalid items in it, it could cause various issues/crashes which are not entirely
        // worth dealing with, as it is relatively reasonable to assume if an item is stored in a slot, more items of that type
        // are valid in the same slot without having to check isItemValid.
        uncheckedSetter.accept(stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        slot.onContentsChanged();
    }

    @Override
    public void onQuickCraft(@Nonnull ItemStack current, @Nonnull ItemStack newStack) {
        int change = newStack.getCount() - current.getCount();
        if (change > 0) {
            slot.onContentsChanged();
            onQuickCraft(newStack, change);
        }
    }

    @Override
    public int getMaxStackSize() {
        return slot.getLimit(ItemStack.EMPTY);
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return slot.getLimit(stack);
    }

    @Override
    public boolean mayPickup(@Nonnull PlayerEntity player) {
        return !slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack remove(int amount) {
        return slot.extractItem(amount, Action.EXECUTE, AutomationType.MANUAL);
    }

    //TODO: Forge has a TODO for implementing isSameInventory.
    // We can compare inventories at the very least for BasicInventorySlots as they have an instance of IMekanismInventory stored
    /*@Override
    public boolean isSameInventory(Slot other) {
        return other instanceof SlotItemHandler && ((SlotItemHandler) other).getItemHandler() == this.itemHandler;
    }*/

    public ContainerSlotType getSlotType() {
        return slotType;
    }

    @Nullable
    public SlotOverlay getSlotOverlay() {
        return slotOverlay;
    }
}