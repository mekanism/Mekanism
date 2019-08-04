package mekanism.common.inventory.container;

import ic2.api.item.IElectricItem;
import javax.annotation.Nonnull;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class ContainerEnergyStorage<TILE extends TileEntityMekanism> extends ContainerMekanism<TILE> {

    protected ContainerEnergyStorage(TILE tile, InventoryPlayer inventory) {
        super(tile, inventory);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (ChargeUtils.canBeCharged(slotStack) || ChargeUtils.canBeDischarged(slotStack)) {
                if (slotStack.getItem() == Items.REDSTONE) {
                    if (slotID != 1) {
                        if (!mergeItemStack(slotStack, 1, 2, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (slotID != 1 && slotID != 0) {
                        if (ChargeUtils.canBeDischarged(slotStack)) {
                            if (!mergeItemStack(slotStack, 1, 2, false)) {
                                if (canTransfer(slotStack)) {
                                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                                        return ItemStack.EMPTY;
                                    }
                                }
                            }
                        } else if (canTransfer(slotStack)) {
                            if (!mergeItemStack(slotStack, 0, 1, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    } else if (slotID == 1) {
                        if (canTransfer(slotStack)) {
                            if (!mergeItemStack(slotStack, 0, 1, false)) {
                                if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
                                    return ItemStack.EMPTY;
                                }
                            }
                        } else if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
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

    private boolean canTransfer(ItemStack slotStack) {
        return MekanismUtils.useIC2() && slotStack.getItem() instanceof IElectricItem;
    }
}