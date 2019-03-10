package mekanism.common.inventory.container;

import javax.annotation.Nonnull;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.inventory.slot.SlotSpecific;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerFormulaicAssemblicator extends ContainerMekanism<TileEntityFormulaicAssemblicator> {

    public ContainerFormulaicAssemblicator(InventoryPlayer inventory, TileEntityFormulaicAssemblicator tile) {
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

            if (ChargeUtils.canBeDischarged(slotStack)) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 34, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (slotStack.getItem() instanceof ItemCraftingFormula) {
                if (slotID != 1) {
                    if (!mergeItemStack(slotStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 34, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (slotID >= 2 && slotID <= 19) {
                if (!mergeItemStack(slotStack, 34, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (tileEntity.formula == null || tileEntity.formula
                  .isIngredient(tileEntity.getWorld(), slotStack)) {
                if (!mergeItemStack(slotStack, 2, 20, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotID >= 34 && slotID <= 60) {
                    if (!mergeItemStack(slotStack, 61, inventorySlots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID > 60) {
                    if (!mergeItemStack(slotStack, 34, 60, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 34, inventorySlots.size(), true)) {
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
        addSlotToContainer(new SlotDischarge(tileEntity, 1, 152, 76));
        addSlotToContainer(new SlotSpecific(tileEntity, 2, 6, 26, ItemCraftingFormula.class));

        for (int slotY = 0; slotY < 2; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlotToContainer(new Slot(tileEntity, slotX + slotY * 9 + 3, 8 + slotX * 18, 98 + slotY * 18));
            }
        }

        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 3; slotX++) {
                addSlotToContainer(new Slot(tileEntity, slotX + slotY * 3 + 27, 26 + slotX * 18, 17 + slotY * 18) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return !tileEntity.autoMode;
                    }

                    @Override
                    public boolean canTakeStack(EntityPlayer player) {
                        return !tileEntity.autoMode;
                    }

                    @Override
                    @SideOnly(Side.CLIENT)
                    public boolean isEnabled() {
                        return !tileEntity.autoMode;
                    }
                });
            }
        }

        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 2; slotX++) {
                addSlotToContainer(
                      new SlotOutput(tileEntity, slotX + slotY * 2 + 21, 116 + slotX * 18, 17 + slotY * 18));
            }
        }
    }
}
