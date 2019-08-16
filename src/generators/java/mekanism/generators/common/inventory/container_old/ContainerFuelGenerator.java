package mekanism.generators.common.inventory.container_old;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container_old.ContainerMekanism;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public abstract class ContainerFuelGenerator<TILE extends TileEntityMekanism> extends ContainerMekanism<TILE> {

    protected ContainerFuelGenerator(PlayerInventory inventory, TILE tile) {
        super(tile, inventory);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (ChargeUtils.canBeCharged(slotStack)) {
                if (slotID != 1) {
                    if (!mergeItemStack(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 2, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (tryFuel(slotStack)) {
                return handleFuel(slotStack, slotID);
            } else if (slotID >= 2 && slotID <= 28) {
                if (!mergeItemStack(slotStack, 29, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID > 28) {
                if (!mergeItemStack(slotStack, 2, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
            if (slotStack.getCount() == 0) {
                currentSlot.putStack(ItemStack.EMPTY);
            } else {
                currentSlot.onSlotChanged();
            }
            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            currentSlot.onTake(player, slotStack);
        }
        return stack;
    }

    protected abstract boolean tryFuel(ItemStack slotStack);

    protected ItemStack handleFuel(ItemStack slotStack, int slotID) {
        if (slotID != 0 && slotID != 1) {
            if (!mergeItemStack(slotStack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
            return ItemStack.EMPTY;
        }
        return slotStack;
    }
}