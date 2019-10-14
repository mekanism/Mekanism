package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.ForgeHooks;

public class FuelwoodHeaterContainer extends MekanismTileContainer<TileEntityFuelwoodHeater> {

    public FuelwoodHeaterContainer(int id, PlayerInventory inv, TileEntityFuelwoodHeater tile) {
        super(MekanismContainerTypes.FUELWOOD_HEATER, id, inv, tile);
    }

    public FuelwoodHeaterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFuelwoodHeater.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (ForgeHooks.getBurnTime(slotStack) > 0) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 1, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID >= 1 && slotID <= 27) {
                if (!mergeItemStack(slotStack, 28, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID > 27) {
                if (!mergeItemStack(slotStack, 1, 27, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 1, inventorySlots.size(), true)) {
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
        addSlot(new Slot(tile, 0, 15, 29));
    }
}