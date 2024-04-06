package mekanism.common.capabilities.proxy;

import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.ISidedItemHandler;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyItemHandler extends ProxyHandler implements IItemHandlerModifiable {

    private final ISidedItemHandler inventory;

    public ProxyItemHandler(ISidedItemHandler inventory, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.inventory = inventory;
    }

    @Override
    public int getSlots() {
        return inventory.getSlots(side);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot, side);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return readOnlyInsert() ? stack : inventory.insertItem(slot, stack, side, Action.get(!simulate));
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return readOnlyExtract() ? ItemStack.EMPTY : inventory.extractItem(slot, amount, side, Action.get(!simulate));
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(slot, side);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !readOnly || inventory.isItemValid(slot, stack, side);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (!readOnly) {
            inventory.setStackInSlot(slot, stack, side);
        }
    }
}