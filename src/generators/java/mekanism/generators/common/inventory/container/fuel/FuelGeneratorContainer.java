package mekanism.generators.common.inventory.container.fuel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.ChargeUtils;
import mekanism.generators.common.tile.TileEntityGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public abstract class FuelGeneratorContainer<TILE extends TileEntityGenerator> extends MekanismTileContainer<TILE> {

    protected FuelGeneratorContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv, TILE tile) {
        super(type, id, inv, tile);
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