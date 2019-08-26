package mekanism.common.inventory.container.tile.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.content.filter.IFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public abstract class FilterContainer<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>> extends MekanismTileContainer<TILE> {

    protected FILTER filter;
    protected FILTER origFilter;

    protected FilterContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv, TILE tile, int index) {
        super(type, id, inv, tile);
        if (index >= 0) {
            //TODO: Should this somehow be checked to verify it is the correct type
            origFilter = (FILTER) tile.getFilters().get(index);
        }
        if (origFilter == null) {
            filter = createNewFilter();
        } else {
            filter = origFilter.clone();
        }
    }

    public boolean isNew() {
        return origFilter == null;
    }

    public FILTER getFilter() {
        return filter;
    }

    public FILTER getOrigFilter() {
        return origFilter;
    }

    public abstract FILTER createNewFilter();

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
}