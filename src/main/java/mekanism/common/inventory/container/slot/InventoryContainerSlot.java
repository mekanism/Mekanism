package mekanism.common.inventory.container.slot;

import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.warning.ISupportsWarning;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.world.inventory.StackCopySlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// Like [ResourceHandlerSlot], except directly interacts with the [IInventorySlot] instead
public class InventoryContainerSlot extends StackCopySlot implements ITransactionalSlot {

    private final Consumer<ItemStack> uncheckedSetter;
    private final ContainerSlotType slotType;
    private final BasicInventorySlot slot;
    @Nullable
    private final SlotOverlay slotOverlay;
    @Nullable
    private final Consumer<ISupportsWarning<?>> warningAdder;

    public InventoryContainerSlot(BasicInventorySlot slot, int x, int y, ContainerSlotType slotType, @Nullable SlotOverlay slotOverlay,
          @Nullable Consumer<ISupportsWarning<?>> warningAdder, Consumer<ItemStack> uncheckedSetter) {
        super(0, x, y);
        this.slot = slot;
        this.slotType = slotType;
        this.slotOverlay = slotOverlay;
        this.warningAdder = warningAdder;
        this.uncheckedSetter = uncheckedSetter;
        //TODO - 26.1: Evaluate callers of getItem in cases where it may be an InventoryContainerSlot (such as for the module tweaker)
        // as it is possible we might be relying on being able to edit the item. While StackCopySlot may be able to handle that
        // we also might be missing the setChanged calls it would be relying on
    }

    public IInventorySlot getInventorySlot() {
        return slot;
    }

    public void addWarnings(ISupportsWarning<?> slot) {
        if (warningAdder != null) {
            warningAdder.accept(slot);
        }
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return resource.isEmpty() ? 0 : slot.insert(resource, amount, transaction, AutomationType.MANUAL);
    }

    @Override
    public int extract(Player player, ItemResource resource, int amount, TransactionContext transaction) {
        //TODO - 26.1: Should we be calling mayPickup here or should we trust that if it is overridden then this method also gets overridden?
        // Calling it duplicates the can extract check, but prevent overriders from having to also override this
        // though we don't check mayPlace for the insert method
        return resource.isEmpty() ? 0 : slot.extract(resource, amount, transaction, AutomationType.MANUAL);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemResource currentResource = slot.getResource();
        ItemResource targetResource = ItemResource.of(stack);
        //If there is an item stored that doesn't match the current stack, we need to check if we can extract the current item
        if (!currentResource.isEmpty() && !currentResource.equals(targetResource) && !slot.isCurrentValidForExtraction(AutomationType.MANUAL)) {
            //If we can't, fail
            return false;
        }
        //If we can check if we can insert the item ignoring the current contents
        return slot.isValidForInsertion(targetResource, AutomationType.MANUAL);
    }

    @NotNull
    @Override
    protected ItemStack getStackCopy() {
        return slot.getResource().toStack(slot.getCount());
    }

    @Override
    protected void setStackCopy(@NotNull ItemStack stack) {
        //Note: We have to set the stack in an unchecked manner here, so that if we sync a stack from the server to the client that
        // the client does not think is valid for the stack, it doesn't cause major issues. Additionally, we do this directly in
        // our putStack method rather than having a separate unchecked method, as if some modder is modifying inventories directly
        // for some reason, and the machine has invalid items in it, it could cause various issues/crashes which are not entirely
        // worth dealing with, as it is relatively reasonable to assume if an item is stored in a slot, more items of that type
        // are valid in the same slot without having to check isItemValid.
        uncheckedSetter.accept(stack);
        setChanged();
    }

    protected boolean allowPartialRemoval() {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return slot.getLimit(ItemResource.EMPTY);
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return slot.getLimit(ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return !slot.isEmpty() && slot.isCurrentValidForExtraction(AutomationType.MANUAL);
    }

    @Override
    public boolean isSameInventory(@NotNull Slot other) {
        return other instanceof InventoryContainerSlot rhs && rhs.slot == this.slot;
    }

    public ContainerSlotType getSlotType() {
        return slotType;
    }

    @Nullable
    public SlotOverlay getSlotOverlay() {
        return slotOverlay;
    }

    @NotNull
    @Override
    public Optional<ItemStack> tryRemove(int amount, int maxAmount, @NotNull Player player) {
        if (!allowPartialRemoval()) {
            //Super logic for if we don't allow removing part of the stack
            return super.tryRemove(amount, maxAmount, player);
        } else if (!mayPickup(player)) {
            return Optional.empty();
        }
        //Skip super check about if we can't place the stack back, as we know our remove method supports just removing part of it
        amount = Math.min(amount, maxAmount);
        ItemStack result = remove(amount);
        if (result.isEmpty()) {
            return Optional.empty();
        } else if (getItem().isEmpty()) {
            setByPlayer(ItemStack.EMPTY, result);
        }
        return Optional.of(result);
    }
}