package mekanism.common.inventory.container.tile.fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.FluidContainerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

//TODO: Remove the need for this by better handling transferStackInSlot
public abstract class MekanismFluidContainer<TILE extends TileEntityMekanism> extends MekanismTileContainer<TILE> {

    protected MekanismFluidContainer(@Nullable ContainerType type, int id, @Nullable PlayerInventory inv, TILE tile) {
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
            if (FluidContainerUtils.isFluidContainer(slotStack)) {
                if (slotID != 0 && slotID != 1) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if (slotID >= 2 && slotID <= 28) {
                    if (!mergeItemStack(slotStack, 29, inventorySlots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID > 28) {
                    if (!mergeItemStack(slotStack, 2, 28, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
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
}