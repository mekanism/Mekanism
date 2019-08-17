package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.IGasItem;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.inventory.slot.SlotStorageTank;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class RotaryCondensentratorContainer extends MekanismTileContainer<TileEntityRotaryCondensentrator> {

    public RotaryCondensentratorContainer(int id, PlayerInventory inv, TileEntityRotaryCondensentrator tile) {
        super(MekanismContainerTypes.ROTARY_CONDENSENTRATOR, id, inv, tile);
    }

    public RotaryCondensentratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityRotaryCondensentrator.class));
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
                if (slotID != 4) {
                    if (!mergeItemStack(slotStack, 4, 5, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 5, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (FluidContainerUtils.isFluidContainer(slotStack)) {
                if (slotID != 2 && slotID != 3) {
                    if (!mergeItemStack(slotStack, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 5, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.getItem() instanceof IGasItem) {
                if (slotID != 0 && slotID != 1) {
                    if (((IGasItem) slotStack.getItem()).canProvideGas(slotStack, tile.gasTank.getGas() != null ? tile.gasTank.getGas().getGas() : null)) {
                        if (!mergeItemStack(slotStack, 0, 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (((IGasItem) slotStack.getItem()).canReceiveGas(slotStack, tile.gasTank.getGas() != null ? tile.gasTank.getGas().getGas() : null)) {
                        if (!mergeItemStack(slotStack, 1, 2, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else if (!mergeItemStack(slotStack, 5, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID >= 5 && slotID <= 31) {
                if (!mergeItemStack(slotStack, 32, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID > 31) {
                if (!mergeItemStack(slotStack, 5, 31, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 5, inventorySlots.size(), true)) {
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
        addSlot(new SlotStorageTank(tile, 0, 5, 25));
        addSlot(new SlotStorageTank(tile, 1, 5, 56));
        addSlot(new Slot(tile, 2, 155, 25));
        addSlot(new SlotOutput(tile, 3, 155, 56));
        addSlot(new SlotDischarge(tile, 4, 155, 5));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new RotaryCondensentratorContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.rotary_condensentrator");
    }
}