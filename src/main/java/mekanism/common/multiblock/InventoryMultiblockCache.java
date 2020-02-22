package mekanism.common.multiblock;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.util.Direction;

public abstract class InventoryMultiblockCache<T extends SynchronizedData<T>> extends MultiblockCache<T> implements IMekanismInventory {

    //Note: We don't care about the types here in the cache as it is just for merging purposes, and then we read the slots over anyways
    @Nonnull
    protected List<IInventorySlot> inventorySlots = Arrays.asList(BasicInventorySlot.at(this, 0, 0), BasicInventorySlot.at(this, 0, 0));

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
    }

    @Override
    public void onContentsChanged() {
    }
}