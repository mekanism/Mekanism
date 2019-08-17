package mekanism.common.inventory.container.entity.robit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.FurnaceResultSlot;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.text.ITextComponent;

public class SmeltingRobitContainer extends RobitContainer {

    private int lastCookTime = 0;
    private int lastBurnTime = 0;
    private int lastItemBurnTime = 0;

    public SmeltingRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(MekanismContainerTypes.SMELTING_ROBIT, id, inv, robit);
    }

    public SmeltingRobitContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getEntityFromBuf(buf, EntityRobit.class));
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, entity.furnaceCookTime);
        listener.sendWindowProperty(this, 1, entity.furnaceBurnTime);
        listener.sendWindowProperty(this, 2, entity.currentItemBurnTime);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : listeners) {
            if (lastCookTime != entity.furnaceCookTime) {
                listener.sendWindowProperty(this, 0, entity.furnaceCookTime);
            }
            if (lastBurnTime != entity.furnaceBurnTime) {
                listener.sendWindowProperty(this, 1, entity.furnaceBurnTime);
            }
            if (lastItemBurnTime != entity.currentItemBurnTime) {
                listener.sendWindowProperty(this, 2, entity.currentItemBurnTime);
            }
        }
        lastCookTime = entity.furnaceCookTime;
        lastBurnTime = entity.furnaceBurnTime;
        lastItemBurnTime = entity.currentItemBurnTime;
    }

    @Override
    public void updateProgressBar(int i, int j) {
        if (i == 0) {
            entity.furnaceCookTime = j;
        }
        if (i == 1) {
            entity.furnaceBurnTime = j;
        }
        if (i == 2) {
            entity.currentItemBurnTime = j;
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
                } else if (FurnaceTileEntity.isFuel(slotStack)) {
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
        addSlot(new Slot(entity, 28, 56, 17));
        addSlot(new Slot(entity, 29, 56, 53));
    }

    @Override
    protected void addInventorySlots(@Nonnull PlayerInventory inventory) {
        addSlot(new FurnaceResultSlot(inventory.player, entity, 30, 116, 35));
        super.addInventorySlots(inventory);
    }
}