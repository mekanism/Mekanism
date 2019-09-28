package mekanism.common.inventory.container.tile.advanced;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public abstract class AdvancedElectricMachineContainer<TILE extends TileEntityAdvancedElectricMachine> extends MekanismTileContainer<TILE> {

    protected AdvancedElectricMachineContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv, TILE tile) {
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
            if (slotID == 2) {
                if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ChargeUtils.canBeDischarged(slotStack)) {
                if (slotID != 3) {
                    if (!mergeItemStack(slotStack, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!tile.getItemGas(slotStack).isEmpty()) {
                if (slotID != 1) {
                    if (!mergeItemStack(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (isInputItem(slotStack)) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID >= 4 && slotID <= 30) {
                if (!mergeItemStack(slotStack, 31, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID > 30) {
                if (!mergeItemStack(slotStack, 4, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
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

    private boolean isInputItem(ItemStack itemstack) {
        return tile.containsRecipe(recipe -> recipe.getItemInput().testType(itemstack));
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 0, 56, 17));
        addSlot(new Slot(tile, 1, 56, 53));
        addSlot(new SlotOutput(tile, 2, 116, 35));
        addSlot(new SlotDischarge(tile, 3, 31, 35));
    }
}