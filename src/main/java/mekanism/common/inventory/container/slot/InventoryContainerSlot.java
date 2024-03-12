package mekanism.common.inventory.container.slot;

import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.warning.ISupportsWarning;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//Like net.minecraftforge.items.SlotItemHandler, except directly interacts with the IInventorySlot instead
public class InventoryContainerSlot extends Slot implements IInsertableSlot {

    private static final Container emptyInventory = new SimpleContainer(0);
    private final Consumer<ItemStack> uncheckedSetter;
    private final ContainerSlotType slotType;
    private final BasicInventorySlot slot;
    @Nullable
    private final SlotOverlay slotOverlay;
    @Nullable
    private final Consumer<ISupportsWarning<?>> warningAdder;

    public InventoryContainerSlot(BasicInventorySlot slot, int x, int y, ContainerSlotType slotType, @Nullable SlotOverlay slotOverlay,
          @Nullable Consumer<ISupportsWarning<?>> warningAdder, Consumer<ItemStack> uncheckedSetter) {
        super(emptyInventory, 0, x, y);
        this.slot = slot;
        this.slotType = slotType;
        this.slotOverlay = slotOverlay;
        this.warningAdder = warningAdder;
        this.uncheckedSetter = uncheckedSetter;
    }

    public IInventorySlot getInventorySlot() {
        return slot;
    }

    public void addWarnings(ISupportsWarning<?> slot) {
        if (warningAdder != null) {
            warningAdder.accept(slot);
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(@NotNull ItemStack stack, Action action) {
        ItemStack remainder = slot.insertItem(stack, action, AutomationType.MANUAL);
        if (action.execute() && stack.getCount() != remainder.getCount()) {
            setChanged();
        }
        return remainder;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
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

    protected boolean allowPartialRemoval() {
        return true;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return slot.getStack();
    }

    @Override
    public boolean hasItem() {
        return !slot.isEmpty();
    }

    @Override
    public void set(@NotNull ItemStack stack) {
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
    public int getMaxStackSize() {
        return slot.getLimit(ItemStack.EMPTY);
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return slot.getLimit(stack);
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return !slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).isEmpty();
    }

    @NotNull
    @Override
    public ItemStack remove(int amount) {
        return slot.extractItem(amount, Action.EXECUTE, AutomationType.MANUAL);
    }

    //TODO: Forge has a TODO for implementing isSameInventory.
    // We can compare inventories at the very least for BasicInventorySlots as they have an instance of IMekanismInventory stored
    /*@Override
    public boolean isSameInventory(Slot other) {
        return other instanceof SlotItemHandler handler && handler.getItemHandler() == this.itemHandler;
    }*/

    public ContainerSlotType getSlotType() {
        return slotType;
    }

    @Nullable
    public SlotOverlay getSlotOverlay() {
        return slotOverlay;
    }

    @NotNull
    @Override
    public Optional<ItemStack> tryRemove(int count, int decrement, @NotNull Player player) {
        if (allowPartialRemoval()) {
            if (!mayPickup(player)) {
                return Optional.empty();
            }
            //Skip super check about if we can't place the stack back, as we know our remove method supports just removing part of it
            count = Math.min(count, decrement);
            ItemStack itemstack = remove(count);
            if (itemstack.isEmpty()) {
                return Optional.empty();
            } else if (getItem().isEmpty()) {
                setByPlayer(ItemStack.EMPTY, itemstack);
            }
            return Optional.of(itemstack);
        }
        //Super logic for if we don't allow removing part of the stack
        return super.tryRemove(count, decrement, player);
    }
}