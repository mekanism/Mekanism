package mekanism.common.inventory.container.tile.filter;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class FilterContainer<TILE extends TileEntityMekanism> extends MekanismTileContainer<TILE> {

    public FilterContainer(int id, PlayerInventory inv, TILE tile) {
        super(MekanismContainerTypes.FILTER, id, inv, tile);
    }

    public FilterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        //TODO: Make this safer? Or extend filter container if the name is needed and then only have this constructor there
        this(id, inv, (TILE) getTileFromBuf(buf, TileEntityMekanism.class));
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
        //TODO: Filter type??
        return TextComponentUtil.translate("mekanism.container.filter");
    }
}