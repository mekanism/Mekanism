package mekanism.common.inventory;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.item.IItemSustainedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;

/**
 * Helper class for implementing handling of inventories for items
 */
public abstract class ItemStackMekanismInventory implements IMekanismInventory {

    private final List<IInventorySlot> slots;
    @Nonnull
    protected final ItemStack stack;

    protected ItemStackMekanismInventory(@Nonnull ItemStack stack) {
        this.stack = stack;
        this.slots = getInitialInventory();
        if (stack.getItem() instanceof IItemSustainedInventory) {
            ListNBT inventory = ((IItemSustainedInventory) stack.getItem()).getInventory(stack);
            int size = slots.size();
            for (int slot = 0; slot < inventory.size(); slot++) {
                CompoundNBT tagCompound = inventory.getCompound(slot);
                byte slotID = tagCompound.getByte("Slot");
                if (slotID >= 0 && slotID < size) {
                    //TODO: Re-evaluate the slot id stuff
                    slots.get(slotID).deserializeNBT(tagCompound);
                }
            }
        }
    }

    protected abstract List<IInventorySlot> getInitialInventory();

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return slots;
    }

    @Override
    public void onContentsChanged() {
        if (stack.getItem() instanceof IItemSustainedInventory) {
            ListNBT tagList = new ListNBT();
            for (int slot = 0; slot < slots.size(); slot++) {
                IInventorySlot inventorySlot = slots.get(slot);
                CompoundNBT tagCompound = inventorySlot.serializeNBT();
                if (!tagCompound.isEmpty()) {
                    //TODO: Re-evaluate how the slot works like this
                    tagCompound.putByte("Slot", (byte) slot);
                    tagList.add(tagCompound);
                }
            }
            ((IItemSustainedInventory) stack.getItem()).setInventory(tagList, stack);
        }
    }
}