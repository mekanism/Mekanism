package mekanism.common.capabilities.proxy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.inventory.ISidedItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandlerModifiable;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProxyItemHandler implements IItemHandlerModifiable {

    private final ISidedItemHandler inventory;
    @Nullable
    private final Direction side;
    private final boolean readOnly;

    //TODO: Should this take a supplier for the item handler in case it somehow gets invalidated??
    public ProxyItemHandler(ISidedItemHandler inventory, @Nullable Direction side) {
        this.inventory = inventory;
        this.side = side;
        this.readOnly = this.side == null;
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
        if (readOnly) {
            return stack;
        }
        return inventory.insertItem(slot, stack, side, Action.get(!simulate));
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (readOnly) {
            return ItemStack.EMPTY;
        }
        return inventory.extractItem(slot, amount, side, Action.get(!simulate));
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