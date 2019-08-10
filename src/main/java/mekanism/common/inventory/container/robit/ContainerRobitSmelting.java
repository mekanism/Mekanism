package mekanism.common.inventory.container.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.FurnaceResultSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.FurnaceTileEntity;

public class ContainerRobitSmelting extends ContainerRobit {

    private int lastCookTime = 0;
    private int lastBurnTime = 0;
    private int lastItemBurnTime = 0;

    public ContainerRobitSmelting(PlayerInventory inventory, EntityRobit entity) {
        super(entity, inventory);
    }

    @Override
    public void addListener(IContainerListener icrafting) {
        super.addListener(icrafting);
        icrafting.sendWindowProperty(this, 0, robit.furnaceCookTime);
        icrafting.sendWindowProperty(this, 1, robit.furnaceBurnTime);
        icrafting.sendWindowProperty(this, 2, robit.currentItemBurnTime);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : listeners) {
            if (lastCookTime != robit.furnaceCookTime) {
                listener.sendWindowProperty(this, 0, robit.furnaceCookTime);
            }
            if (lastBurnTime != robit.furnaceBurnTime) {
                listener.sendWindowProperty(this, 1, robit.furnaceBurnTime);
            }
            if (lastItemBurnTime != robit.currentItemBurnTime) {
                listener.sendWindowProperty(this, 2, robit.currentItemBurnTime);
            }
        }
        lastCookTime = robit.furnaceCookTime;
        lastBurnTime = robit.furnaceBurnTime;
        lastItemBurnTime = robit.currentItemBurnTime;
    }

    @Override
    public void updateProgressBar(int i, int j) {
        if (i == 0) {
            robit.furnaceCookTime = j;
        }
        if (i == 1) {
            robit.furnaceBurnTime = j;
        }
        if (i == 2) {
            robit.currentItemBurnTime = j;
        }
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
                if (!mergeItemStack(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID != 1 && slotID != 0) {
                if (!FurnaceRecipes.instance().getSmeltingResult(slotStack).isEmpty()) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (FurnaceTileEntity.isItemFuel(slotStack)) {
                    if (!mergeItemStack(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID < 30) {
                    if (!mergeItemStack(slotStack, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID < 39 && !mergeItemStack(slotStack, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 3, 39, false)) {
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

    @Override
    protected void addSlots() {
        addSlot(new Slot(robit, 28, 56, 17));
        addSlot(new Slot(robit, 29, 56, 53));
    }

    @Override
    protected void addInventorySlots(PlayerInventory inventory) {
        addSlot(new FurnaceResultSlot(inventory.player, robit, 30, 116, 35));
        super.addInventorySlots(inventory);
    }
}