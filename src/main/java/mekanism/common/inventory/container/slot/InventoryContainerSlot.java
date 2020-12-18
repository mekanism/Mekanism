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
            onSlotChanged();
        }
        return remainder;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (slot.isEmpty()) {
            //If the slot is currently empty, just try simulating the insertion
            return insertItem(stack, Action.SIMULATE).getCount() < stack.getCount();
        }
        //Otherwise we need to check if we can extract the current item
        if (slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).isEmpty()) {
            //If we can't, fail
            return false;
        }
        //If we can check if we can insert the item ignoring the current contents
        return slot.isItemValidForInsertion(stack, AutomationType.MANUAL);
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        return slot.getStack();
    }

    @Override
    public boolean getHasStack() {
        return !slot.isEmpty();
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        //Note: We have to set the stack in an unchecked manor here, so that if we sync a stack from the server to the client that
        // the client does not think is valid for the stack, it doesn't cause major issues
        uncheckedSetter.accept(stack);
        onSlotChanged();
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        slot.onContentsChanged();
    }

    @Override
    public void onSlotChange(@Nonnull ItemStack current, @Nonnull ItemStack newStack) {
        int change = newStack.getCount() - current.getCount();
        if (change > 0) {
            slot.onContentsChanged();
            onCrafting(newStack, change);
        }
    }

    @Override
    public int getSlotStackLimit() {
        return slot.getLimit(ItemStack.EMPTY);
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return slot.getLimit(stack);
    }

    @Override
    public boolean canTakeStack(@Nonnull PlayerEntity player) {
        return !slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) {
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