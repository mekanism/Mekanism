package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.api.gas.IGasItem;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityGasTank;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class GasTankContainer extends MekanismTileContainer<TileEntityGasTank> {

    public GasTankContainer(int id, PlayerInventory inv, TileEntityGasTank tile) {
        super(MekanismContainerTypes.GAS_TANK, id, inv, tile);
    }

    public GasTankContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityGasTank.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotStack.getItem() instanceof IGasItem) {
                if (slotID != 0 && slotID != 1) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        if (!mergeItemStack(slotStack, 1, 2, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else if (!mergeItemStack(slotStack, 2, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
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
}