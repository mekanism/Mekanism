package mekanism.common.lib.inventory;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.common.Mekanism;
import mekanism.common.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;

public class TileTransitRequest extends TransitRequest {

    private final TileEntity tile;
    private final Direction side;
    private final Map<HashedItem, TileItemData> itemMap = new LinkedHashMap<>();

    public TileTransitRequest(TileEntity tile, Direction side) {
        this.tile = tile;
        this.side = side;
    }

    public void addItem(ItemStack stack, int slot) {
        HashedItem hashed = new HashedItem(stack);
        itemMap.computeIfAbsent(hashed, TileItemData::new).addSlot(slot, stack);
    }

    public int getCount(HashedItem itemType) {
        ItemData data = itemMap.get(itemType);
        return data != null ? data.getTotalCount() : 0;
    }

    public Map<HashedItem, TileItemData> getItemMap() {
        return itemMap;
    }

    @Override
    public Collection<TileItemData> getItemData() {
        return itemMap.values();
    }

    public class TileItemData extends ItemData {

        private final Int2IntMap slotMap = new Int2IntOpenHashMap();

        public TileItemData(HashedItem itemType) {
            super(itemType);
        }

        public void addSlot(int id, ItemStack stack) {
            slotMap.put(id, stack.getCount());
            totalCount += stack.getCount();
        }

        @Override
        public ItemStack use(int amount) {
            IItemHandler handler = InventoryUtils.assertItemHandler("TileTransitRequest", tile, side);
            if (handler != null) {
                ObjectIterator<Int2IntMap.Entry> iterator = slotMap.int2IntEntrySet().iterator();
                while (iterator.hasNext()) {
                    Int2IntMap.Entry entry = iterator.next();
                    int currentCount = entry.getIntValue();
                    int toUse = Math.min(amount, currentCount);
                    ItemStack ret = handler.extractItem(entry.getIntKey(), toUse, false);
                    boolean stackable = InventoryUtils.areItemsStackable(getItemType().getStack(), ret);
                    if (!stackable || ret.getCount() != toUse) { // be loud if an InvStack's prediction doesn't line up
                        Mekanism.logger.warn("An inventory's returned content {} does not line up with TileTransitRequest's prediction.", !stackable ? "type" : "count");
                        Mekanism.logger.warn("TileTransitRequest item: {}, toUse: {}, ret: {}", getItemType().getStack(), toUse, ret);
                        Mekanism.logger.warn("Tile: {} {}", tile.getType().getRegistryName(), tile.getPos());
                    }
                    amount -= toUse;
                    totalCount -= toUse;
                    if (totalCount == 0) {
                        itemMap.remove(getItemType());
                    }
                    entry.setValue(currentCount = currentCount - toUse);
                    if (currentCount == 0) {
                        //If we removed all items from this slot, remove the slot
                        iterator.remove();
                    }
                    if (amount == 0) {
                        break;
                    }
                }
            }
            return getStack();
        }
    }
}