package mekanism.common.inventory.container;

import javax.annotation.Nonnull;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.tile.TileEntityChanceMachine;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerChanceMachine extends ContainerMekanism<TileEntityChanceMachine> {

    public ContainerChanceMachine(InventoryPlayer inventory, TileEntityChanceMachine tile) {
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

            if (slotID == 2 || slotID == 3) {
                if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (ChargeUtils.canBeDischarged(slotStack)) {
                if (slotID != 1) {
                    if (!mergeItemStack(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (RecipeHandler.getChanceRecipe(new ItemStackInput(slotStack), tileEntity.getRecipes()) != null) {
                if (slotID != 0 && slotID != 1) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if (slotID >= 4 && slotID <= 30) {
                    if (!mergeItemStack(slotStack, 31, inventorySlots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID > 30) {
                    if (!mergeItemStack(slotStack, 4, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
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

    @Override
    protected void addSlots() {
        addSlotToContainer(new Slot(tileEntity, 0, 56, 17));
        addSlotToContainer(new SlotDischarge(tileEntity, 1, 56, 53));
        addSlotToContainer(new SlotOutput(tileEntity, 2, 116, 35));
        addSlotToContainer(new SlotOutput(tileEntity, 4, 132, 35));
    }
}
