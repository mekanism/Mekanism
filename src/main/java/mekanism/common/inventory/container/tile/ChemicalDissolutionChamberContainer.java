package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.api.gas.IGasItem;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotStorageTank;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class ChemicalDissolutionChamberContainer extends MekanismTileContainer<TileEntityChemicalDissolutionChamber> {

    public ChemicalDissolutionChamberContainer(int id, PlayerInventory inv, TileEntityChemicalDissolutionChamber tile) {
        super(MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER, id, inv, tile);
    }

    public ChemicalDissolutionChamberContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityChemicalDissolutionChamber.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (tile.containsRecipe(recipe -> recipe.getItemInput().testType(slotStack))) {
                if (slotID != 1) {
                    if (!mergeItemStack(slotStack, 1, 2, true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
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
            } else if (slotStack.getItem() instanceof IGasItem) {
                if (slotID != 0 && slotID != 2) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        if (!mergeItemStack(slotStack, 2, 3, false)) {
                            return ItemStack.EMPTY;
                        }
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

    @Override
    protected void addSlots() {
        addSlot(new SlotStorageTank(tile, 0, 6, 65));
        addSlot(new Slot(tile, 1, 26, 36));
        addSlot(new SlotStorageTank(tile, 2, 155, 25));
        addSlot(new SlotDischarge(tile, 3, 155, 5));
    }
}