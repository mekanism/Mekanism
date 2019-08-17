package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.IGasItem;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotStorageTank;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class ChemicalInfuserContainer extends MekanismTileContainer<TileEntityChemicalInfuser> {

    public ChemicalInfuserContainer(int id, PlayerInventory inv, TileEntityChemicalInfuser tile) {
        super(MekanismContainerTypes.CHEMICAL_INFUSER, id, inv, tile);
    }

    public ChemicalInfuserContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityChemicalInfuser.class));
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
                if (slotID != 3) {
                    if (!mergeItemStack(slotStack, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.getItem() instanceof IGasItem) {
                if (slotID != 0 && slotID != 1 && slotID != 2) {
                    if (!mergeItemStack(slotStack, 0, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 5, inventorySlots.size(), true)) {
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
        addSlot(new SlotStorageTank(tile, 0, 5, 56));
        addSlot(new SlotStorageTank(tile, 1, 155, 56));
        addSlot(new SlotStorageTank(tile, 2, 80, 65));
        addSlot(new SlotDischarge(tile, 3, 155, 5));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new ChemicalInfuserContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.chemical_infuser");
    }
}