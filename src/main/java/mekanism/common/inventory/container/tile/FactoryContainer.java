package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotExtra;
import mekanism.common.inventory.slot.SlotInput;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemHandlerHelper;

public class FactoryContainer extends MekanismTileContainer<TileEntityFactory> {

    public FactoryContainer(int id, PlayerInventory inv, TileEntityFactory tile) {
        super(MekanismContainerTypes.FACTORY, id, inv, tile);
    }

    public FactoryContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFactory.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new SlotDischarge(tile, 1, 7, 13));
        //TODO: Mark these two side slots to not be auto generated gui images for
        addSlot(new Slot(tile, 2, 180, 75));
        addSlot(new Slot(tile, 3, 180, 112));
        addSlot(new SlotExtra(tile, 4, 7, 57));
        //TODO: Ultimate factory move other slots around, and shift these leftwards
        int baseX = tile.tier == FactoryTier.BASIC ? 55 : tile.tier == FactoryTier.ADVANCED ? 35 : tile.tier == FactoryTier.ELITE ? 29 : 10;
        int baseXMult = tile.tier == FactoryTier.BASIC ? 38 : tile.tier == FactoryTier.ADVANCED ? 26 : 19;
        for (int i = 0; i < tile.tier.processes; i++) {
            addSlot(new FactoryInputSlot(tile, getInputSlotIndex(i), baseX + (i * baseXMult), 13, i));
        }
        for (int i = 0; i < tile.tier.processes; i++) {
            addSlot(new SlotOutput(tile, getOutputSlotIndex(i), baseX + (i * baseXMult), 57));
        }
    }

    @Override
    protected int getInventoryOffset() {
        return 95;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (isOutputSlot(slotID)) {
                if (!mergeItemStack(slotStack, tile.getSizeInventory() - 1, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID != 1 && slotID != 2 && isProperMachine(slotStack) && !ItemHandlerHelper.canItemStacksStack(slotStack, tile.getMachineStack())) {
                if (!mergeItemStack(slotStack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID == 2) {
                if (!mergeItemStack(slotStack, tile.getSizeInventory() - 1, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (tile.isValidInputItem(slotStack)) {
                if (isInputSlot(slotID)) {
                    if (!mergeItemStack(slotStack, tile.getSizeInventory() - 1, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, 4 + tile.tier.processes, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (ChargeUtils.canBeDischarged(slotStack)) {
                if (slotID == 0) {
                    if (!mergeItemStack(slotStack, tile.getSizeInventory() - 1, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (tile.isValidExtraItem(slotStack)) {
                //TODO: Should this check it is the extra slot before checking if it is a valid input?
                if (transferExtraSlot(slotID, slotStack)) {
                    return ItemStack.EMPTY;
                }
            } else {
                int slotEnd = tile.getSizeInventory() - 1;
                if (slotID >= slotEnd && slotID <= (slotEnd + 26)) {
                    if (!mergeItemStack(slotStack, slotEnd + 27, inventorySlots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID > (slotEnd + 26)) {
                    if (!mergeItemStack(slotStack, slotEnd, slotEnd + 26, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, slotEnd, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
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

    private boolean transferExtraSlot(int slotID, ItemStack slotStack) {
        if (slotID >= tile.getSizeInventory() - 1) {
            return !mergeItemStack(slotStack, 3, 4, false);
        }
        return !mergeItemStack(slotStack, tile.getSizeInventory() - 1, inventorySlots.size(), true);
    }

    public boolean isProperMachine(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            for (RecipeType type : RecipeType.values()) {
                if (ItemHandlerHelper.canItemStacksStack(itemStack, type.getStack())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInputSlot(int slot) {
        return slot >= 4 && slot < 4 + tile.tier.processes;
    }

    public boolean isOutputSlot(int slot) {
        return slot >= 4 + tile.tier.processes && slot < 4 + tile.tier.processes * 2;
    }

    private int getOutputSlotIndex(int processNumber) {
        return tile.tier.processes + getInputSlotIndex(processNumber);
    }

    private int getInputSlotIndex(int processNumber) {
        return 5 + processNumber;
    }

    private class FactoryInputSlot extends SlotInput {

        /**
         * The index of the processes slot. 0 <= processNumber < tileEntity.tier.processes For matching the input to output slot
         */
        private final int processNumber;

        private FactoryInputSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, int processNumber) {
            super(inventoryIn, index, xPosition, yPosition);
            this.processNumber = processNumber;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            ItemStack outputSlotStack = tile.getInventory().get(getOutputSlotIndex(this.processNumber));
            return tile.inputProducesOutput(getInputSlotIndex(this.processNumber), stack, outputSlotStack, false) && super.isItemValid(stack);
        }
    }
}