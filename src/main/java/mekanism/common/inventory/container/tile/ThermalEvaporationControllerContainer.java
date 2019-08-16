package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class ThermalEvaporationControllerContainer extends MekanismTileContainer<TileEntityThermalEvaporationController> {

    public ThermalEvaporationControllerContainer(int id, PlayerInventory inv, TileEntityThermalEvaporationController tile) {
        super(MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER, id, inv, tile);
    }

    public ThermalEvaporationControllerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityThermalEvaporationController.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            //TODO: Do this better
            LazyOptionalHelper<FluidStack> fluidHelper = new LazyOptionalHelper<>(FluidUtil.getFluidContained(slotStack));
            if (slotID == 1 || slotID == 3) {
                if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (FluidContainerUtils.isFluidContainer(slotStack) && !fluidHelper.isPresent()) {
                if (slotID != 2) {
                    if (!mergeItemStack(slotStack, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (fluidHelper.isPresent() && tile.hasRecipe(fluidHelper.getValue().getFluid())) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
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
        addSlot(new Slot(tile, 0, 28, 20));
        addSlot(new SlotOutput(tile, 1, 28, 51));
        addSlot(new Slot(tile, 2, 132, 20));
        addSlot(new SlotOutput(tile, 3, 132, 51));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.thermal_evaporation_controller");
    }
}