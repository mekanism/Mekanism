package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidUtil;

public class ElectricPumpContainer extends MekanismTileContainer<TileEntityElectricPump> {

    public ElectricPumpContainer(int id, PlayerInventory inv, TileEntityElectricPump tile) {
        super(MekanismContainerTypes.ELECTRIC_PUMP, id, inv, tile);
    }

    public ElectricPumpContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityElectricPump.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (ChargeUtils.canBeDischarged(slotStack)) {
                if (slotID != 2) {
                    if (!mergeItemStack(slotStack, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 3, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (FluidContainerUtils.isFluidContainer(slotStack) && !FluidUtil.getFluidContained(slotStack).isPresent()) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 3, inventorySlots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (slotID == 1) {
                if (!mergeItemStack(slotStack, 3, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotID >= 3 && slotID <= 29) {
                    if (!mergeItemStack(slotStack, 30, inventorySlots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotID > 29) {
                    if (!mergeItemStack(slotStack, 3, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!mergeItemStack(slotStack, 3, inventorySlots.size(), true)) {
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
        addSlot(new Slot(tile, 0, 28, 20));
        addSlot(new SlotOutput(tile, 1, 28, 51));
        addSlot(new SlotDischarge(tile, 2, 143, 35));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.electric_pump");
    }
}