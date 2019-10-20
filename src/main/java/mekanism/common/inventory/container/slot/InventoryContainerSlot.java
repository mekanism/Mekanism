package mekanism.common.inventory.container.slot;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.util.StackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

//Like net.minecraftforge.items.SlotItemHandler, except directly interacts with the IInventorySlot instead
//TODO: Override other methods to pass them directly to our IInventorySlot
public class InventoryContainerSlot extends Slot implements IInsertableSlot {

    private static IInventory emptyInventory = new Inventory(0);
    private final ContainerSlotType slotType;
    private final IInventorySlot slot;

    public InventoryContainerSlot(IInventorySlot slot, int x, int y, ContainerSlotType slotType) {
        super(emptyInventory, 0, x, y);
        this.slot = slot;
        this.slotType = slotType;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(@Nonnull ItemStack stack, Action action) {
        ItemStack remainder = slot.insertItem(stack, action);
        if (action.execute() && stack.getCount() != remainder.getCount()) {
            onSlotChanged();
        }
        return remainder;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        //TODO: Should this also check canInsert to make sure that it does not get manually inserted
        return !stack.isEmpty() && slot.isItemValid(stack);
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        //TODO: Does this need to return a copy? Depends on if this getStack is allowed to be modified
        return slot.getStack();
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        slot.setStack(stack);
        onSlotChanged();
    }

    @Override
    public void onSlotChange(@Nonnull ItemStack current, @Nonnull ItemStack newStack) {
        //TODO: should we call: slot.onContentsChanged();
    }

    @Override
    public int getSlotStackLimit() {
        return slot.getLimit(ItemStack.EMPTY);
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        //TODO: This logic is a lot simpler than the one in SlotItemHandler, is there some case we are missing?
        return slot.getLimit(stack);
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        //TODO: Switch to some variation of !slot.extractItem(1, Action.SIMULATE).isEmpty();
        // See decrStackSize for more details
        return slot.shrinkStack(1, Action.SIMULATE) == 1;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) {
        //TODO: Can we use some variation of slot.extractItem(amount, Action.EXECUTE);
        // Currently we have to use shrink as we have extraction disabled (FOR AUTOMATION), maybe we can instead make some extract method that allows bypassing the automation check
        ItemStack stack = slot.getStack();
        if (slot.shrinkStack(amount, Action.EXECUTE) != amount) {
            //TODO: Print error that something went wrong??
        }
        return StackUtils.size(stack, amount);
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
}