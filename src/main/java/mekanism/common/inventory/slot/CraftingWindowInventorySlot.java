package mekanism.common.inventory.slot;

import java.util.function.BiPredicate;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import net.minecraft.item.ItemStack;

public class CraftingWindowInventorySlot extends BasicInventorySlot {

    //TODO: is having no listener correct or will we actually want to have one for purposes of marking
    // things as saved
    public static CraftingWindowInventorySlot input(int tableIndex, int slotIndex) {
        return new CraftingWindowInventorySlot(notExternal, alwaysTrueBi, tableIndex, slotIndex);
    }

    public static CraftingWindowInventorySlot output(int tableIndex, int slotIndex) {
        return new CraftingWindowInventorySlot(manualOnly, internalOnly, tableIndex, slotIndex);
    }

    private final int tableIndex;
    private final int slotIndex;

    private CraftingWindowInventorySlot(BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert, int tableIndex, int slotIndex) {
        super(canExtract, canInsert, alwaysTrue, null, 0, 0);
        this.tableIndex = tableIndex;
        this.slotIndex = slotIndex;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public SyncableItemStack createSyncableItemStack() {
        //Sets the stack in an unchecked manner so that it always properly works on the client
        return SyncableItemStack.create(this::getStack, this::setStackUnchecked);
    }
}