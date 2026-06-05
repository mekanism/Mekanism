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

    private final ContainerSlotType slotType;
    private final BasicInventorySlot slot;
    @Nullable
    private final SlotOverlay slotOverlay;
    @Nullable
    private final Consumer<ISupportsWarning<?>> warningAdder;

    public InventoryContainerSlot(BasicInventorySlot slot, int x, int y, ContainerSlotType slotType, @Nullable SlotOverlay slotOverlay,
          @Nullable Consumer<ISupportsWarning<?>> warningAdder) {
        super(0, x, y);
        this.slot = slot;
        this.slotType = slotType;
        this.slotOverlay = slotOverlay;
        this.warningAdder = warningAdder;
        //TODO - 26.1: Evaluate callers of getItem in cases where it may be an InventoryContainerSlot
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
        ItemResource currentResource = slot.resource();
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
        return slot.resource().toStack(slot.amountAsInt());
    }

    @Override
    protected void setStackCopy(@NotNull ItemStack stack) {
        slot.setContents(ItemResource.of(stack), stack.count(), null);
    }

    protected boolean allowPartialRemoval() {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return slot.capacityAsInt(ItemResource.EMPTY);
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return slot.capacityAsInt(ItemResource.of(stack));
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