package mekanism.common.capabilities.proxy;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyItemHandler extends ProxyHandler implements IItemHandlerModifiable {

    private final IMekanismInventory inventory;

    public ProxyItemHandler(IMekanismInventory inventory, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.inventory = inventory;
    }

    @Override
    public int getSlots() {
        return inventory.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return readOnlyInsert() ? stack : inventory.insertItem(slot, stack, Action.get(!simulate), AutomationType.handler(side));
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return readOnlyExtract() ? ItemStack.EMPTY : inventory.extractItem(slot, amount, Action.get(!simulate), AutomationType.handler(side));
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !readOnly || inventory.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (!readOnly) {
            inventory.setStackInSlot(slot, stack);
        }
    }
}