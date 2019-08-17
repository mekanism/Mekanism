package mekanism.generators.common.inventory.container.turbine;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class TurbineContainer extends MekanismTileContainer<TileEntityTurbineCasing> {

    public TurbineContainer(int id, PlayerInventory inv, TileEntityTurbineCasing tile) {
        super(GeneratorsContainerTypes.INDUSTRIAL_TURBINE, id, inv, tile);
    }

    public TurbineContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityTurbineCasing.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotID <= 26) {
                if (!mergeItemStack(slotStack, 27, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 0, 26, false)) {
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

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanismgenerators.container.industrial_turbine");
    }
}